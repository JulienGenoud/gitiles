// Copyright (C) 2014 Google Inc. All Rights Reserved.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package com.google.gitiles;

import static com.google.gitiles.DateFormatter.Format.DEFAULT;
import static com.google.gitiles.DateFormatter.Format.ISO;
import static java.util.TimeZone.getTimeZone;
import static org.junit.Assert.assertEquals;

import com.google.common.base.Optional;

import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.util.GitDateParser;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.text.ParseException;
import java.util.Date;
import java.util.TimeZone;

@RunWith(JUnit4.class)
public class DateFormatterTest {
  @Test
  public void defaultIncludingTimeZone() throws Exception {
    PersonIdent ident = newIdent("Mon Jan 2 15:04:05 2006", "-0700");
    DateFormatter df = new DateFormatter(Optional.<TimeZone> absent(), DEFAULT);
    assertEquals("Mon Jan 02 15:04:05 2006 -0700", df.format(ident));
  }

  @Test
  public void defaultWithUtc() throws Exception {
    PersonIdent ident = newIdent("Mon Jan 2 15:04:05 2006", "-0700");
    DateFormatter df = new DateFormatter(Optional.of(getTimeZone("UTC")), DEFAULT);
    assertEquals("Mon Jan 02 22:04:05 2006", df.format(ident));
  }

  @Test
  public void defaultWithOtherTimeZone() throws Exception {
    PersonIdent ident = newIdent("Mon Jan 2 15:04:05 2006", "-0700");
    DateFormatter df = new DateFormatter(Optional.of(getTimeZone("GMT-0400")), DEFAULT);
    assertEquals("Mon Jan 02 18:04:05 2006", df.format(ident));
  }

  @Test
  public void isoIncludingTimeZone() throws Exception {
    PersonIdent ident = newIdent("Mon Jan 2 15:04:05 2006", "-0700");
    DateFormatter df = new DateFormatter(Optional.<TimeZone> absent(), ISO);
    assertEquals("2006-01-02 15:04:05 -0700", df.format(ident));
  }

  @Test
  public void isoWithUtc() throws Exception {
    PersonIdent ident = newIdent("Mon Jan 2 15:04:05 2006", "-0700");
    DateFormatter df = new DateFormatter(Optional.of(getTimeZone("UTC")), ISO);
    assertEquals("2006-01-02 22:04:05", df.format(ident));
  }

  @Test
  public void isoWithOtherTimeZone() throws Exception {
    PersonIdent ident = newIdent("Mon Jan 2 15:04:05 2006", "-0700");
    DateFormatter df = new DateFormatter(Optional.of(getTimeZone("GMT-0400")), ISO);
    assertEquals("2006-01-02 18:04:05", df.format(ident));
  }

  private PersonIdent newIdent(String whenStr, String tzStr) throws ParseException {
    whenStr += " " + tzStr;
    Date when = GitDateParser.parse(whenStr, null);
    TimeZone tz = getTimeZone("GMT" + tzStr);
    PersonIdent ident = new PersonIdent("A User", "user@example.com", when, tz);
    // PersonIdent.toString() uses its own format with "d" instead of "dd",
    // hence the mismatches in 2 vs. 02 above. Nonetheless I think this sanity
    // check is useful enough to keep around.
    assertEquals("PersonIdent[A User, user@example.com, " + whenStr + "]", ident.toString());
    return ident;
  }
}