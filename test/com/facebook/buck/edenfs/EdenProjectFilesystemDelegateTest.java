/*
 * Copyright (c) Facebook, Inc. and its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.facebook.buck.edenfs;

import static org.easymock.EasyMock.createMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.easymock.EasyMock.verify;
import static org.junit.Assert.assertEquals;

import com.facebook.buck.io.filesystem.ProjectFilesystemDelegate;
import com.facebook.buck.io.filesystem.impl.DefaultProjectFilesystemDelegate;
import com.facebook.buck.util.config.Config;
import com.facebook.buck.util.config.ConfigBuilder;
import com.facebook.buck.util.sha1.Sha1HashCode;
import com.facebook.eden.thrift.EdenError;
import com.facebook.thrift.TException;
import com.google.common.base.Strings;
import com.google.common.hash.Hashing;
import com.google.common.jimfs.Configuration;
import com.google.common.jimfs.Jimfs;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystem;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.UserDefinedFileAttributeView;
import java.util.Optional;
import org.junit.Test;

public class EdenProjectFilesystemDelegateTest {
  private static final Sha1HashCode DUMMY_SHA1 = Sha1HashCode.of(Strings.repeat("faceb00c", 5));

  /**
   * This is the location of the working directory for {@link Configuration#unix()}. Creating
   * symlinks via {@link Files#createSymbolicLink(Path, Path,
   * java.nio.file.attribute.FileAttribute[])} in the working directory of Jimfs does not touch the
   * actual filesystem.
   */
  private static final String JIMFS_WORKING_DIRECTORY = "/work";

  @Test
  public void computeSha1ForOrdinaryFileUnderMount() throws IOException, EdenError, TException {
    FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);

    EdenMount mount = createMock(EdenMount.class);
    Path path = fs.getPath("foo/bar");
    expect(mount.getPathRelativeToProjectRoot(root.resolve(path))).andReturn(Optional.of(path));
    expect(mount.getSha1(path)).andReturn(DUMMY_SHA1);
    replay(mount);

    EdenProjectFilesystemDelegate edenDelegate = new EdenProjectFilesystemDelegate(mount, delegate);
    assertEquals(DUMMY_SHA1, edenDelegate.computeSha1(path));

    verify(mount);
  }

  @Test
  public void computeSha1ForSymlinkUnderMountThatPointsToFileUnderMount()
      throws EdenError, TException, IOException {
    FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);

    // Create a symlink within the project root.
    Path link = fs.getPath("/work/link");
    Path target = fs.getPath("/work/target");
    Files.createFile(target);
    Files.createSymbolicLink(link, target);

    // Eden will throw when the SHA-1 for the link is requested, but return a SHA-1 when the target
    // is requested.
    EdenMount mount = createMock(EdenMount.class);
    expect(mount.getPathRelativeToProjectRoot(link)).andReturn(Optional.of(fs.getPath("link")));
    expect(mount.getPathRelativeToProjectRoot(target)).andReturn(Optional.of(fs.getPath("target")));
    expect(mount.getSha1(fs.getPath("link"))).andThrow(new EdenError());
    expect(mount.getSha1(fs.getPath("target"))).andReturn(DUMMY_SHA1);
    replay(mount);

    EdenProjectFilesystemDelegate edenDelegate = new EdenProjectFilesystemDelegate(mount, delegate);
    assertEquals(DUMMY_SHA1, edenDelegate.computeSha1(link));

    verify(mount);
  }

  @Test
  public void computeSha1ForSymlinkUnderMountThatPointsToFileOutsideMount()
      throws IOException, EdenError, TException {
    FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);

    // Create a symlink within the project root.
    Path link = fs.getPath("/work/link");
    Path target = fs.getPath("/example");
    Files.createFile(target);
    byte[] bytes = new byte[] {66, 85, 67, 75};
    Files.write(target, bytes);
    Files.createSymbolicLink(link, target);

    // Eden will throw when the SHA-1 for the link is requested, but return a SHA-1 when the target
    // is requested.
    EdenMount mount = createMock(EdenMount.class);
    expect(mount.getPathRelativeToProjectRoot(link)).andReturn(Optional.of(fs.getPath("link")));
    expect(mount.getPathRelativeToProjectRoot(target)).andReturn(Optional.empty());
    expect(mount.getSha1(fs.getPath("link"))).andThrow(new EdenError());
    replay(mount);

    EdenProjectFilesystemDelegate edenDelegate = new EdenProjectFilesystemDelegate(mount, delegate);
    assertEquals(
        "EdenProjectFilesystemDelegate.computeSha1() should return the SHA-1 of the target of "
            + "the symlink even though the target is outside of the EdenFS root.",
        Sha1HashCode.fromHashCode(Hashing.sha1().hashBytes(bytes)),
        edenDelegate.computeSha1(link));

    verify(mount);
  }

  @Test
  public void computeSha1ForOrdinaryFileOutsideMount() throws IOException {
    FileSystem fs = Jimfs.newFileSystem(Configuration.unix());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);
    Path target = fs.getPath("/example");
    Files.createFile(target);
    byte[] bytes = new byte[] {66, 85, 67, 75};
    Files.write(target, bytes);

    EdenMount mount = createMock(EdenMount.class);
    expect(mount.getPathRelativeToProjectRoot(target)).andReturn(Optional.empty());
    replay(mount);

    EdenProjectFilesystemDelegate edenDelegate = new EdenProjectFilesystemDelegate(mount, delegate);
    assertEquals(
        "EdenProjectFilesystemDelegate.computeSha1() should return the SHA-1 of a file that is "
            + "outside of the EdenFS root.",
        Sha1HashCode.fromHashCode(Hashing.sha1().hashBytes(bytes)),
        edenDelegate.computeSha1(target));

    verify(mount);
  }

  @Test
  public void computeSha1ViaXattrForFileUnderMount() throws IOException {
    FileSystem fs =
        Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("user").build());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);

    Path path = fs.getPath("/foo");
    Files.createFile(path);
    UserDefinedFileAttributeView view =
        Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);

    ByteBuffer buf = ByteBuffer.wrap(DUMMY_SHA1.toString().getBytes(StandardCharsets.UTF_8));
    view.write("sha1", buf);
    EdenMount mount = createMock(EdenMount.class);
    Config config = ConfigBuilder.createFromText("[eden]", "use_xattr = true");
    EdenProjectFilesystemDelegate edenDelegate =
        new EdenProjectFilesystemDelegate(mount, delegate, config);
    assertEquals(DUMMY_SHA1, edenDelegate.computeSha1(path));
  }

  @Test
  public void computeSha1ViaXattrForFileUnderMountInvalidUTF8() throws IOException {
    FileSystem fs =
        Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("user").build());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);

    Path path = fs.getPath("/foo");
    Files.createFile(path);
    byte[] bytes = new byte[] {66, 85, 67, 75};
    Files.write(path, bytes);
    UserDefinedFileAttributeView view =
        Files.getFileAttributeView(path, UserDefinedFileAttributeView.class);

    ByteBuffer buf = ByteBuffer.allocate(2);
    buf.putChar((char) 0xfffe);
    view.write("sha1", buf);
    EdenMount mount = createMock(EdenMount.class);
    Config config = ConfigBuilder.createFromText("[eden]", "use_xattr = true");
    EdenProjectFilesystemDelegate edenDelegate =
        new EdenProjectFilesystemDelegate(mount, delegate, config);
    assertEquals(
        "EdenProjectFilesystemDelegate.computeSha1() should return the SHA-1 of the contents",
        Sha1HashCode.fromHashCode(Hashing.sha1().hashBytes(bytes)),
        edenDelegate.computeSha1(path));
  }

  @Test
  public void computeSha1ViaXattrForFileOutsideMount() throws IOException {
    FileSystem fs =
        Jimfs.newFileSystem(Configuration.unix().toBuilder().setAttributeViews("user").build());
    Path root = fs.getPath(JIMFS_WORKING_DIRECTORY);
    ProjectFilesystemDelegate delegate = new DefaultProjectFilesystemDelegate(root);

    Path path = fs.getPath("/foo");
    Files.createFile(path);
    byte[] bytes = new byte[] {66, 85, 67, 75};
    Files.write(path, bytes);

    EdenMount mount = createMock(EdenMount.class);
    Config config = ConfigBuilder.createFromText("[eden]", "use_xattr = true");
    EdenProjectFilesystemDelegate edenDelegate =
        new EdenProjectFilesystemDelegate(mount, delegate, config);
    assertEquals(
        "EdenProjectFilesystemDelegate.computeSha1() should return the SHA-1 of a file that is "
            + "outside of the EdenFS root.",
        Sha1HashCode.fromHashCode(Hashing.sha1().hashBytes(bytes)),
        edenDelegate.computeSha1(path));
  }
}
