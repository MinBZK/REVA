/*
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for additional information regarding
 * copyright ownership. The ASF licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License. You may obtain a
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package nl.overheid.stelsel.digimelding.astore.remote.soap.adapters;

import java.util.List;
import java.util.Set;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.clerezza.rdf.core.Language;
import org.apache.clerezza.rdf.core.PlainLiteral;
import org.apache.clerezza.rdf.core.Resource;
import org.apache.clerezza.rdf.core.TypedLiteral;
import org.apache.clerezza.rdf.core.UriRef;
import org.apache.clerezza.rdf.core.sparql.ResultSet;
import org.apache.clerezza.rdf.core.sparql.SolutionMapping;
import org.apache.clerezza.rdf.core.sparql.query.Variable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSSerializer;

/**
 * JAXB XML Adapter to convert from ResultSet to String.
 * 
 */
public class ResultSetAdapter extends XmlAdapter<String, ResultSet> {

  final Logger logger = LoggerFactory.getLogger(ResultSetAdapter.class);

  // -------------------------------------------------------------------------
  // Implementing XmlAdapter
  // -------------------------------------------------------------------------

  @Override
  public String marshal(ResultSet resultSet) throws Exception {
    Document doc = toXmlSource(resultSet);
    DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
    LSSerializer lsSerializer = domImplementation.createLSSerializer();
    return lsSerializer.writeToString(doc);
  }

  @Override
  public ResultSet unmarshal(String resultSetString) throws Exception {
    // DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    // Document doc = dbf.newDocumentBuilder().newDocument();
    // DOMImplementationLS domImplementation = (DOMImplementationLS) doc.getImplementation();
    // LSParser lsParser = domImplementation.createLSParser( DOMImplementationLS.MODE_SYNCHRONOUS,
    // null );
    // LSInput lsInput = domImplementation.createLSInput();
    // lsInput.setStringData( resultSetString );
    // Document resultSetDocument = lsParser.parse( lsInput );

    // ResultSets are used one way only.
    return null;
  }

  // -------------------------------------------------------------------------
  // Private methods
  // -------------------------------------------------------------------------

  /**
   * Helper: transforms a {@link ResultSet} or a {@link Boolean} to a {@link Document}
   *
   * @param queryResult
   * @param query
   * @param applyStyle
   */
  private Document toXmlSource(ResultSet queryResult) {
    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    try {
      Document doc = dbf.newDocumentBuilder().newDocument();
      // adding root element
      Element root = doc.createElement("sparql");
      root.setAttribute("xmlns", "http://www.w3.org/2005/sparql-results#");
      doc.appendChild(root);
      Element head = doc.createElement("head");
      createVariables(queryResult.getResultVars(), head, doc);
      root.appendChild(head);

      Element results = doc.createElement("results");
      while (queryResult.hasNext()) {
        createResultElement(queryResult.next(), results, doc);
      }
      root.appendChild(results);

      return doc;

    } catch (ParserConfigurationException e) {
      throw createWebApplicationException(e);
    }
  }

  /**
   * Creates a WebApplicationexception and prints a logger entry
   */
  private WebApplicationException createWebApplicationException(Exception e) {
    return new WebApplicationException(Response.status(Status.BAD_REQUEST)
        .entity(e.getMessage().replace("<", "&lt;").replace("\n", "<br/>")).build());
  }


  /**
   * Helper: creates value element from {@link Resource} depending on its class
   *
   */
  private Element createValueElement(Resource resource, Document doc) {
    Element value = null;
    if (resource instanceof UriRef) {
      value = doc.createElement("uri");
      value.appendChild(doc.createTextNode(((UriRef) resource).getUnicodeString()));
    } else if (resource instanceof TypedLiteral) {
      value = doc.createElement("literal");
      value.appendChild(doc.createTextNode(((TypedLiteral) resource).getLexicalForm()));
      value.setAttribute("datatype", (((TypedLiteral) resource).getDataType().getUnicodeString()));
    } else if (resource instanceof PlainLiteral) {
      value = doc.createElement("literal");
      value.appendChild(doc.createTextNode(((PlainLiteral) resource).getLexicalForm()));
      Language lang = ((PlainLiteral) resource).getLanguage();
      if (lang != null) {
        value.setAttribute("xml:lang", (lang.toString()));
      }
    } else {
      value = doc.createElement("bnode");
      value.appendChild(doc.createTextNode("/"));
    }
    return value;
  }

  /**
   * Helper: creates results element from ResultSet
   *
   */
  private void createResultElement(SolutionMapping solutionMap, Element results, Document doc) {
    Set<Variable> keys = solutionMap.keySet();
    Element result = doc.createElement("result");
    results.appendChild(result);
    for (Variable key : keys) {
      Element bindingElement = doc.createElement("binding");
      bindingElement.setAttribute("name", key.getName());
      bindingElement.appendChild(createValueElement((Resource) solutionMap.get(key), doc));
      result.appendChild(bindingElement);
    }
  }

  private void createVariables(List<String> variables, Element head, Document doc) {
    for (String variable : variables) {
      Element varElement = doc.createElement("variable");
      varElement.setAttribute("name", variable);
      head.appendChild(varElement);
    }
  }
}
