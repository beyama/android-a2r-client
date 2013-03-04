package eu.addicted2random.a2rclient.jsonrpc;

import java.io.IOException;

import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Abstract base class of all JSON-RPC 2 messages.
 * 
 * @author Alexander Jentz, beyama.de
 * 
 */
public abstract class Message {

  static class MessageDeserializer extends StdDeserializer<Message> {

    private static final long serialVersionUID = 1L;

    protected MessageDeserializer() {
      super(Message.class);
    }

    @Override
    public Message deserialize(JsonParser parser, DeserializationContext ctx) throws IOException,
        JsonProcessingException {

      ObjectMapper mapper = (ObjectMapper) parser.getCodec();
      ObjectNode root = mapper.readTree(parser);

      if (root.has("result")) {
        return mapper.treeToValue(root, Result.class);
      } else if (root.has("error")) {
        return mapper.treeToValue(root, Error.class);
      } else if (root.has("method")) {
        return mapper.treeToValue(root, Request.class);
      }

      return null;
    }
  }

  private static final ObjectMapper mapper = new ObjectMapper();

  static {
    mapper.setVisibility(PropertyAccessor.ALL, Visibility.NONE);
    mapper.setVisibility(PropertyAccessor.FIELD, Visibility.NONE);

    SimpleModule module = new SimpleModule();
    module.addDeserializer(Message.class, new MessageDeserializer());

    mapper.registerModule(module);
  }

  /**
   * Get the {@link ObjectMapper} used to serialize/deserialize messages.
   * 
   * @return
   */
  public static ObjectMapper getMapper() {
    return mapper;
  }

  /**
   * Get a new instance of {@link Message} from JSON string.
   * 
   * @param json
   * @return
   * @throws JsonParseException
   * @throws JsonMappingException
   * @throws IOException
   */
  public static Message fromJson(String json) throws JsonParseException, JsonMappingException, IOException {
    return mapper.readValue(json, Message.class);
  }

  /**
   * Generate a JSON string for a {@link Message}
   * 
   * @param message
   * @return
   * @throws JsonProcessingException
   */
  public static String toJsonString(Message message) throws JsonProcessingException {
    return mapper.writeValueAsString(message);
  }

  private static final String JSONRPC = "2.0";

  @JsonProperty
  private final String jsonrpc = JSONRPC;

  /**
   * Convenience method to check if this message is a {@link Response}.
   * 
   * @return
   */
  public boolean isResponse() {
    return this instanceof Response;
  }

  /**
   * Convenience method to check if this message is a {@link Result}.
   * 
   * @return
   */
  public boolean isResult() {
    return this instanceof Result;
  }

  /**
   * Convenience method to check if this message is an {@link Error}.
   * 
   * @return
   */
  public boolean isError() {
    return this instanceof Error;
  }

  /**
   * Convenience method to check if this message is a {@link Request}.
   * 
   * @return
   */
  public boolean isRequest() {
    return this instanceof Request;
  }

  /**
   * Convenience method to cast this message to a {@link Result}.
   * 
   * @return Returns null if this message is not a {@link Result}
   */
  public Result asResult() {
    if (!isResult())
      return null;
    return (Result) this;
  }

  /**
   * Convenience method to cast this message to a {@link Error}.
   * 
   * @return Returns null if this message is not a {@link Error}
   */
  public Error asError() {
    if (!isError())
      return null;
    return (Error) this;
  }

  /**
   * Convenience method to cast this message to a {@link Request}.
   * 
   * @return Returns null if this message is not a {@link Request}
   */
  public Request asRequest() {
    if (!isRequest())
      return null;
    return (Request) this;
  }

}
