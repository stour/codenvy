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
package com.codenvy.api.permission.server.dao;

import com.codenvy.api.permission.server.Permissions;

import org.bson.BsonReader;
import org.bson.BsonWriter;
import org.bson.Document;
import org.bson.codecs.Codec;
import org.bson.codecs.DecoderContext;
import org.bson.codecs.EncoderContext;
import org.bson.codecs.configuration.CodecRegistry;

import java.util.List;

/**
 * Encodes & decodes {@link Permissions}.
 *
 * @author Sergii Leschenko
 */
public class PermissionsCodec implements Codec<Permissions> {

    private Codec<Document> codec;

    public PermissionsCodec(CodecRegistry registry) {
        codec = registry.get(Document.class);
    }

    @Override
    public Permissions decode(BsonReader reader, DecoderContext decoderContext) {
        final Document document = codec.decode(reader, decoderContext);

        @SuppressWarnings("unchecked") // 'actions' fields is aways list
        final List<String> actions = (List<String>)document.get("actions");

        return new Permissions(document.getString("user"),
                               document.getString("domain"),
                               document.getString("instance"),
                               actions);
    }

    @Override
    public void encode(BsonWriter writer, Permissions permissions, EncoderContext encoderContext) {
        final Document document = new Document().append("user", permissions.getUser())
                                                .append("domain", permissions.getDomain())
                                                .append("instance", permissions.getInstance())
                                                .append("actions", permissions.getActions());

        codec.encode(writer, document, encoderContext);
    }

    @Override
    public Class<Permissions> getEncoderClass() {
        return Permissions.class;
    }
}
