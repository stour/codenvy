/*
 *  [2012] - [2016] Codenvy, S.A.
 *  All Rights Reserved.
 *
 * NOTICE:  All information contained herein is, and remains
 * the property of Codenvy S.A. and its suppliers,
 * if any.  The intellectual and technical concepts contained
 * herein are proprietary to Codenvy S.A.
 * and its suppliers and may be covered by U.S. and Foreign Patents,
 * patents in process, and are protected by trade secret or copyright law.
 * Dissemination of this information or reproduction of this material
 * is strictly forbidden unless prior written permission is obtained
 * from Codenvy S.A..
 */
package com.codenvy.machine.authentication.ide;

import com.codenvy.machine.authentication.shared.dto.MachineTokenDto;
import com.google.gwt.core.client.Callback;
import com.google.gwt.http.client.RequestBuilder;

import org.eclipse.che.api.promises.client.Operation;
import org.eclipse.che.api.promises.client.OperationException;
import org.eclipse.che.api.promises.client.Promise;
import org.eclipse.che.api.promises.client.PromiseError;
import org.eclipse.che.api.promises.client.callback.CallbackPromiseHelper;
import org.eclipse.che.api.promises.client.js.Executor;
import org.eclipse.che.api.promises.client.js.Promises;
import org.eclipse.che.api.promises.client.js.RejectFunction;
import org.eclipse.che.api.promises.client.js.ResolveFunction;
import org.eclipse.che.ide.rest.AsyncRequest;
import org.eclipse.che.ide.rest.AsyncRequestCallback;
import org.eclipse.che.ide.rest.Unmarshallable;
import org.eclipse.che.ide.util.loging.Log;

/**
 * @author Anton Korneta
 */
public class MachineAsyncRequest extends AsyncRequest {

    private final MachineTokenServiceClient machineTokenServiceClient;

    protected MachineAsyncRequest(RequestBuilder.Method method,
                                  String url,
                                  boolean async,
                                  MachineTokenServiceClient machineTokenServiceClient) {
        super(method, url, async);
        this.machineTokenServiceClient = machineTokenServiceClient;
    }

    @Override
    public Promise<Void> send() {
        final Executor.ExecutorBody<Void> body = new Executor.ExecutorBody<Void>() {
            @Override
            public void apply(final ResolveFunction<Void> resolve, final RejectFunction reject) {
                machineTokenServiceClient.getMachineToken()
                                         .then(new Operation<MachineTokenDto>() {
                                             @Override
                                             public void apply(MachineTokenDto machineTokenDto) throws OperationException {
                                                 MachineAsyncRequest.this.user("codenvy").password(machineTokenDto.getMachineToken());
                                             }
                                         });
            }
        };
        final Executor<Void> executor = Executor.create(body);
        return Promises.create(executor);
    }

    @Override
    public void send(final AsyncRequestCallback<?> callback) {
        machineTokenServiceClient.getMachineToken()
                                 .then(new Operation<MachineTokenDto>() {
                                     @Override
                                     public void apply(MachineTokenDto machineTokenDto) throws OperationException {
                                         MachineAsyncRequest.this.user("codenvy").password(machineTokenDto.getMachineToken());
                                         MachineAsyncRequest.super.send(callback);
                                     }
                                 })
                                 .catchError(new Operation<PromiseError>() {
                                     @Override
                                     public void apply(PromiseError arg) throws OperationException {
                                         Log.error(getClass(), arg);
                                     }
                                 });
    }

    @Override
    public <R> Promise<R> send(final Unmarshallable<R> unmarshaller) {
        return CallbackPromiseHelper.createFromCallback(new CallbackPromiseHelper.Call<R, Throwable>() {
            @Override
            public void makeCall(final Callback<R, Throwable> callback) {
                send(new AsyncRequestCallback<R>(unmarshaller) {
                    @Override
                    protected void onSuccess(R result) {
                        callback.onSuccess(result);
                    }

                    @Override
                    protected void onFailure(Throwable exception) {
                        callback.onFailure(exception);
                    }
                });
            }
        });
    }
}
