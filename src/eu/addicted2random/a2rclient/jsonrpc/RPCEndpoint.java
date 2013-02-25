package eu.addicted2random.a2rclient.jsonrpc;


/**
 * Interface for all JSON-RPC request endpoints.
 * 
 * @author Alexander Jentz, beyama.de
 *
 */
public interface RPCEndpoint {
  Response call(Request request);
}
