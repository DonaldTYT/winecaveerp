package com.kikyosoft.graphql;


import graphql.language.FieldDefinition;
import graphql.language.ObjectTypeDefinition;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

import java.io.InputStream;
import java.util.Optional;

public class SchemaReader {
private final TypeDefinitionRegistry registry;

public SchemaReader(String p_filepath) {
 InputStream in = getClass().getResourceAsStream(p_filepath);
 if (in == null) throw new IllegalStateException("schema.graphql not on classpath");
 this.registry = new SchemaParser().parse(in);
}

public Optional<ObjectTypeDefinition> object(String name) {
 return registry.getType(name, ObjectTypeDefinition.class);
}

//public static void main(String[] args) { // optional quick check
// SchemaReader r = new SchemaReader();
// r.object("Category").ifPresent(cat -> {
//   for (FieldDefinition f : cat.getFieldDefinitions()) {
//     System.out.println(f.getName() + " : " + f.getType());
//   }
// });
//}
}
