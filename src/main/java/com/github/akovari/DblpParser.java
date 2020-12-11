package com.github.akovari;

//
// Copyright (c)2015, dblp Team (University of Trier and
// Schloss Dagstuhl - Leibniz-Zentrum fuer Informatik GmbH)
// All rights reserved.
//
// Redistribution and use in source and binary forms, with or without
// modification, are permitted provided that the following conditions are met:
//
// (1) Redistributions of source code must retain the above copyright
// notice, this list of conditions and the following disclaimer.
//
// (2) Redistributions in binary form must reproduce the above copyright
// notice, this list of conditions and the following disclaimer in the
// documentation and/or other materials provided with the distribution.
//
// (3) Neither the name of the dblp team nor the names of its contributors
// may be used to endorse or promote products derived from this software
// without specific prior written permission.
//
// THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
// ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
// WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
// DISCLAIMED. IN NO EVENT SHALL DBLP TEAM BE LIABLE FOR ANY
// DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
// (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
// LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
// ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
// (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
// SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
//

import me.tongfei.progressbar.ProgressBar;
import org.dblp.mmdb.*;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;


@SuppressWarnings("javadoc")
class DblpParser {

  public static void main(String[] args) throws IOException {

    // we need to raise entityExpansionLimit because the dblp.xml has millions of entities
    System.setProperty("entityExpansionLimit", "10000000");

    String dblpXmlFilename = "data/dblp.xml";
    String dblpDtdFilename = "data/dblp.dtd";

    System.out.println("building the dblp main memory DB ...");
    RecordDbInterface dblp;
    try {
      dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);
    } catch (final IOException ex) {
      System.err.println("cannot read dblp XML: " + ex.getMessage());
      return;
    } catch (final SAXException ex) {
      System.err.println("cannot parse XML: " + ex.getMessage());
      return;
    }
    System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());

    PrintWriter publicationsWriter = new PrintWriter("data/publications.json", StandardCharsets.UTF_8);
    for (Publication publication1 : ProgressBar.wrap(dblp.getPublications(), "publications")) {
      JSONObject jo = new JSONObject();
      jo.put("key", publication1.getKey());
      if (publication1.getPublicationStream() != null) {
        jo.put("title", publication1.getPublicationStream().getTitle());
      }
      jo.put("year", publication1.getYear());
      if (publication1.getJournal() != null) {
        jo.put("journal_title", publication1.getJournal().getTitle());
      }
      if (publication1.getBooktitle() != null) {
        jo.put("book_title", publication1.getBooktitle().getTitle());
      }
      if (publication1.getToc() != null) {
        jo.put("toc_key", publication1.getToc().getKey());
      }
      jo.put("mdate", publication1.getMdate());

      Map<String, JSONArray> publicationIds = new HashMap<>();
      for (PublicationIDType publicationIDType : publication1.getIdTypes()) {
        publicationIds.put(publicationIDType.label(), new JSONArray(publication1.getIds(publicationIDType)));
      }

      jo.put("ids", new JSONObject(publicationIds));

      publicationsWriter.println(jo);
    }

    PrintWriter tocWriter = new PrintWriter("data/tocs.json", StandardCharsets.UTF_8);
    for (TableOfContents toc : ProgressBar.wrap(dblp.getTocs(), "TOCs")) {
      JSONObject jo = new JSONObject();
      jo.put("key", toc.getKey());
      jo.put("page_url", toc.getPageUrl());

      tocWriter.println(jo);
    }

    PrintWriter personsWriter = new PrintWriter("data/persons.json", StandardCharsets.UTF_8);
    for (Person person : ProgressBar.wrap(dblp.getPersons(), "persons")) {
      JSONObject jo = new JSONObject();
      jo.put("key", person.getKey());
      jo.put("primary_name", person.getPrimaryName());
      jo.put("pid", person.getPid());
      jo.put("most_recent_mdate", person.getAggregatedMdate());
      jo.put("mdate", person.getMdate());
      jo.put("number_of_publications", person.numberOfPublications());
      jo.put("publication_ids", new JSONArray(person.getPublications().stream().map(Publication::getKey).collect(Collectors.toUnmodifiableList())));
      jo.put("number_of_coauthors", dblp.numberOfCoauthors(person));

      Map<String, JSONArray> personIds = new HashMap<>();
      for (PersonIDType personIDType : person.getIdTypes()) {
        personIds.put(personIDType.label(), new JSONArray(person.getIds(personIDType)));
      }

      jo.put("ids", new JSONObject(personIds));

      personsWriter.println(jo);
    }
  }
}
