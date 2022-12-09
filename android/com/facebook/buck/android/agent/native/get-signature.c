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

#include <stdlib.h>
#include <stdio.h>
#include <string.h>
#include <ctype.h>

#define MINIZ_HEADER_FILE_ONLY
#include "miniz.c"
#include "miniz-extensions.h"

static int match_signature(void* userdata, const char* name) {
  (void)userdata;
  const char* prefix = "META-INF/";
  const char* suffix = ".SF";
  int prefix_len = strlen(prefix);
  int suffix_len = strlen(suffix);
  int name_len = strlen(name);
  int body_len = name_len - prefix_len - suffix_len;
  int suffix_offset = prefix_len + body_len;

  if (!(body_len > 0)) {
    return 0;
  }

  if (memcmp(prefix, name, prefix_len)) {
    return 0;
  }
  if (memcmp(suffix, name + suffix_offset, suffix_len)) {
    return 0;
  }

  for (int i = prefix_len; i < suffix_offset; i++) {
    if (!isupper(name[i])) {
      return 0;
    }
  }

  return 1;
}

// Returns 0 on success.
int print_digest_manifest(const char* zip_file_name) {
  size_t size;
  char* contents = mzx_extract_match_to_heap(
      zip_file_name,
      match_signature,
      NULL,
      &size,
      0);
  if (contents == NULL) {
    perror("failed to extract signature");
    return 1;
  }
  if (size == 0) {
    fprintf(stderr, "Empty signature file.\n");
    return 1;
  }

  // Force the file contents to be null-terminated so we can use string
  // functions.  It is a text file, so we don't expect any premature nulls.
  // I don't think the manifest digest line is ever the last line, so we
  // shouldn't be causing any unnecessary failures here.
  contents[size-1] = '\0';

  int success = 0;

  for (
      const char* position = contents;
      position != NULL;
      position = strchr(position, '\n')) {

    // Position always points to a newline at this point, so by incrementing
    // it, we point to the first character of the next line, or the
    // null terminator that we added.
    position++;

    const char* prefix_sha1 = "SHA1-Digest-Manifest: ";
    const char* prefix_sha256 = "SHA-256-Digest-Manifest: ";
    int prefix_sha1_len = strlen(prefix_sha1);
    int prefix_sha256_len = strlen(prefix_sha256);
    int prefix_len = -1;
    if (strncmp(position, prefix_sha1, prefix_sha1_len) == 0) {
      prefix_len = prefix_sha1_len;
    }

    if (strncmp(position, prefix_sha256, prefix_sha256_len) == 0) {
      prefix_len = prefix_sha256_len;
    }

    if(prefix_len == -1) {
      continue;
    }

    const char* endpoint = strchr(position, '\n');
    if (endpoint == NULL) {
      continue;
    }
    success = 1;
    int digest_len = (endpoint - position) - prefix_len;
    printf("%.*s\n", digest_len, position + prefix_len);
    break;
  }

  free(contents);
  return !success;
}


int do_get_signature(int num_args, char** args) {
  if (num_args != 1) {
    fprintf(stderr, "usage: get-signature FILE\n");
    return 1;
  }

  return print_digest_manifest(args[0]);
}
