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
package com.codenvy.api.dao.mongo;

import com.codenvy.api.permission.server.PermissionsImpl;
import com.codenvy.api.workspace.server.WorkspaceAction;
import com.codenvy.api.workspace.server.model.WorkerImpl;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Encodes & decodes {@link PermissionsImpl}.
 *
 * @author Sergii Leschenko
 */
public class WorkerImplCodec implements Codec<WorkerImpl> {

    private Codec<Document> codec;

    public WorkerImplCodec(CodecRegistry registry) {
        codec = registry.get(Document.class);
    }

    @Override
    public WorkerImpl decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);

        @SuppressWarnings("unchecked") // 'actions' fields is aways list
        final List<String> actions = (List<String>)document.get("actions");

        return new WorkerImpl(document.getString("user"),
                              document.getString("workspace"),
                              actions.stream()
                                     .map(WorkspaceAction::getEnum)
                                     .collect(Collectors.toList()));
    }

    @Override
    public void encode(BsonWriter writer, WorkerImpl permissions, EncoderContext encoderContext) {
        final Document document = new Document().append("user", permissions.getUser())
                                                .append("workspace", permissions.getWorkspace())
                                                .append("actions", permissions.getActions()
                                                                              .stream()
                                                                              .map(WorkspaceAction::toString)
                                                                              .collect(Collectors.toList()));

        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<WorkerImpl> getEncoderClass() {
        return WorkerImpl.class;
    }
}
