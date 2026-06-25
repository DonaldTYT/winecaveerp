package saleorsync;

import graphql.GraphQL;
import graphql.language.Document;
import graphql.parser.Parser;
import graphql.schema.GraphQLScalarType;
import graphql.schema.GraphQLSchema;
import graphql.schema.TypeResolver;
import graphql.schema.idl.*;
import graphql.validation.ValidationError;
import graphql.validation.Validator;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class GraphQLValidationTest {

  private GraphQL buildGraphQLFromSchema() {
    InputStream s = getClass().getResourceAsStream("/schema.graphql");
    if (s == null) {
      throw new IllegalStateException("schema.graphql not found on classpath (src/test/resources/).");
    }

    TypeDefinitionRegistry typeRegistry = new SchemaParser().parse(s);

    RuntimeWiring.Builder wiring = RuntimeWiring.newRuntimeWiring();

    // --- 1) Dummy scalars (names must match SDL) ---
    for (String name : Arrays.asList(
        "Metadata","DateTime","JSONString","Minute","Day","Hour","Date",
        "Decimal","UUID","PositiveDecimal","JSON","WeightScalar","Upload",
        "GenericScalar","_Any"
    )) {
      wiring.scalar(dummyScalar(name));
    }

    // --- 2) No-op type resolvers for interfaces/unions (required at build time) ---
    TypeResolver nullResolver = env -> null; // never used in this test (no execution)
    for (String t : Arrays.asList(
        "Node","ObjectWithMetadata","Job","PromotionEventInterface",
        "PromotionRuleEventInterface","Event","TranslatableItem",
        "CheckoutLineProblem","DeliveryMethod","UserOrApp","CheckoutProblem",
        "PromotionEvent","TaxSourceObject","IssuingPrincipal","TranslationTypes",
        "TaxSourceLine","OrderOrCheckout","_Entity"
    )) {
      wiring.type(TypeRuntimeWiring.newTypeWiring(t).typeResolver(nullResolver));
    }

    GraphQLSchema schema = new SchemaGenerator().makeExecutableSchema(typeRegistry, wiring.build());
    return GraphQL.newGraphQL(schema).build();
  }

  private static GraphQLScalarType dummyScalar(String name) {
    return GraphQLScalarType.newScalar()
        .name(name)
        .description("Dummy scalar for validation only")
        .coercing(new graphql.schema.Coercing<Object, Object>() {
          @Override public Object serialize(Object dataFetcherResult) { return dataFetcherResult; }
          @Override public Object parseValue(Object input) { return input; }
          @Override public Object parseLiteral(Object input) { return input; }
        })
        .build();
  }

  @Test
  void query_is_valid_against_schema() {
    String gql =
        "query Categories($after:String){\n" +
        "  categories(first:100, after:$after){\n" +
        "    pageInfo{ hasNextPage endCursor }\n" +
        "    edges{ node{ id name slug description parent{ id slug } } }\n" +
        "  }\n" +
        "}";

    Document doc = new Parser().parseDocument(gql);
    GraphQL graphQL = buildGraphQLFromSchema();

    List<ValidationError> errors =
        new Validator().validateDocument(graphQL.getGraphQLSchema(), doc);

    assertTrue(errors.isEmpty(), "Validation errors: " + errors);
  }
}
