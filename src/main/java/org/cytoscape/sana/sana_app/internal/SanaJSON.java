package org.cytoscape.sana.sana_app.internal;


import java.util.List;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SanaJSON {

  private CyNetworkViewWriterFactory writerFactory;

  public SanaJSON(CyNetworkViewWriterFactory writerFactory) {
    this.writerFactory = writerFactory;
  }

  public String encode(TaskMonitor taskMonitor, CyNetwork network) throws Exception {
    final ByteArrayOutputStream stream = new ByteArrayOutputStream();
    CyWriter writer = this.writerFactory.createWriter(stream, network);
    if (writer == null){
    	throw new Exception("Unable to write the network to CX");
    }
    String jsonString = null;
    
    try {
      writer.run(taskMonitor);
      jsonString = stream.toString("UTF-8");
      stream.close();
    } catch (Exception e) {
      throw new IOException();
    }
    return jsonString;
  }

  public SanaResponse decode(String json) throws IOException {
    ObjectMapper objectMapper = new ObjectMapper();
    return objectMapper.readValue(json, SanaResponse.class);
  }

}
class SanaResponse{
	private List<Edge> edgeList;
	private final List<Object> errors;
	@JsonCreator
    public SanaResponse(@JsonProperty("edges") final List<Edge> edges, @JsonProperty("errors") final List<Object> errors) {
        this.edgeList = edges;
        this.errors = errors;
    }

    public List<Edge> getEdgeList() {
        return edgeList;
    }
    public List<Object> getErrors(){
    	return errors;
    }
}

class Edge{
	private final long sourceId;
	private final long targetId;
	@JsonCreator
    public Edge(@JsonProperty("s") final Long source,
                 @JsonProperty("t") Long target) {
        this.sourceId = source;
        this.targetId = target;
    }

    public Long getSourceId() {
        return sourceId;
    }

    public Long getTargetId() {
        return targetId;
    }
}
