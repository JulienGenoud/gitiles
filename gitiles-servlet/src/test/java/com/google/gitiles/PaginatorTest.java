// Copyright 2012 Google Inc. All Rights Reserved.
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

import static com.google.common.base.Preconditions.checkArgument;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.eclipse.jgit.internal.storage.dfs.DfsRepository;
import org.eclipse.jgit.internal.storage.dfs.DfsRepositoryDescription;
import org.eclipse.jgit.internal.storage.dfs.InMemoryRepository;
import org.eclipse.jgit.junit.TestRepository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevWalk;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.List;

/** Unit tests for {@link LogServlet}. */
@RunWith(JUnit4.class)
public class PaginatorTest {
  private TestRepository<DfsRepository> repo;
  private RevWalk walk;

  @Before
  public void setUp() throws Exception {
    repo = new TestRepository<DfsRepository>(
        new InMemoryRepository(new DfsRepositoryDescription("test")));
    walk = new RevWalk(repo.getRepository());
  }

  @After
  public void tearDown() throws Exception {
    walk.release();
  }

  @Test
  public void oneResult() throws Exception {
    List<RevCommit> commits = linearCommits(1);
    walk.markStart(commits.get(0));
    Paginator p = new Paginator(walk, 10, null);
    assertEquals(
        ImmutableList.of(commits.get(0)),
        ImmutableList.copyOf(p));
    assertNull(p.getPreviousStart());
    assertNull(p.getNextStart());
  }

  @Test
  public void lessThanOnePage() throws Exception {
    List<RevCommit> commits = linearCommits(3);
    walk.markStart(commits.get(2));
    Paginator p = new Paginator(walk, 10, null);
    assertEquals(
        ImmutableList.of(
          commits.get(2),
          commits.get(1),
          commits.get(0)),
        ImmutableList.copyOf(p));
    assertNull(p.getPreviousStart());
    assertNull(p.getNextStart());
  }

  @Test
  public void exactlyOnePage() throws Exception {
    List<RevCommit> commits = linearCommits(3);
    walk.markStart(commits.get(2));
    Paginator p = new Paginator(walk, 3, null);
    assertEquals(
        ImmutableList.of(
          commits.get(2),
          commits.get(1),
          commits.get(0)),
        ImmutableList.copyOf(p));
    assertNull(p.getPreviousStart());
    assertNull(p.getNextStart());
  }

  @Test
  public void moreThanOnePage() throws Exception {
    List<RevCommit> commits = linearCommits(5);
    walk.markStart(commits.get(4));
    Paginator p = new Paginator(walk, 3, null);
    assertEquals(
        ImmutableList.of(
          commits.get(4),
          commits.get(3),
          commits.get(2)),
        ImmutableList.copyOf(p));
    assertNull(p.getPreviousStart());
    assertEquals(commits.get(1), p.getNextStart());
  }

  @Test
  public void start() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 3, commits.get(9));
    assertEquals(
        ImmutableList.of(
            commits.get(9),
            commits.get(8),
            commits.get(7)),
        ImmutableList.copyOf(p));
    assertNull(p.getPreviousStart());
    assertEquals(commits.get(6), p.getNextStart());
  }

  @Test
  public void noStartCommit() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 3, null);
    assertEquals(
        ImmutableList.of(
            commits.get(9),
            commits.get(8),
            commits.get(7)),
        ImmutableList.copyOf(p));
    assertNull(p.getPreviousStart());
    assertEquals(commits.get(6), p.getNextStart());
  }

  @Test
  public void lessThanOnePageIn() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 3, commits.get(8));
    assertEquals(
        ImmutableList.of(
            commits.get(8),
            commits.get(7),
            commits.get(6)),
        ImmutableList.copyOf(p));
    assertEquals(commits.get(9), p.getPreviousStart());
    assertEquals(commits.get(5), p.getNextStart());
  }

  @Test
  public void atLeastOnePageIn() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 3, commits.get(7));
    assertEquals(
        ImmutableList.of(
            commits.get(7),
            commits.get(6),
            commits.get(5)),
        ImmutableList.copyOf(p));
    assertEquals(commits.get(9), p.getPreviousStart());
    assertEquals(commits.get(4), p.getNextStart());
  }

  @Test
  public void end() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 3, commits.get(2));
    assertEquals(
        ImmutableList.of(
            commits.get(2),
            commits.get(1),
            commits.get(0)),
        ImmutableList.copyOf(p));
    assertEquals(commits.get(5), p.getPreviousStart());
    assertNull(p.getNextStart());
  }

  @Test
  public void onePastEnd() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 3, commits.get(1));
    assertEquals(
        ImmutableList.of(
            commits.get(1),
            commits.get(0)),
        ImmutableList.copyOf(p));
    assertEquals(commits.get(4), p.getPreviousStart());
    assertNull(p.getNextStart());
  }

  @Test
  public void manyPastEnd() throws Exception {
    List<RevCommit> commits = linearCommits(10);
    walk.markStart(commits.get(9));
    Paginator p = new Paginator(walk, 5, commits.get(1));
    assertEquals(
        ImmutableList.of(
            commits.get(1),
            commits.get(0)),
        ImmutableList.copyOf(p));
    assertEquals(commits.get(6), p.getPreviousStart());
    assertNull(p.getNextStart());
  }

  private List<RevCommit> linearCommits(int n) throws Exception {
    checkArgument(n > 0);
    List<RevCommit> commits = Lists.newArrayList();
    commits.add(repo.commit().create());
    for (int i = 1; i < n; i++) {
      commits.add(repo.commit().parent(commits.get(commits.size() - 1)).create());
    }
    return commits;
  }
}
