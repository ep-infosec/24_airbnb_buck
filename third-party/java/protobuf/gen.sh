#!/bin/bash
# Copyright 2019-present Facebook, Inc.
#
# Licensed under the Apache License, Version 2.0 (the "License"); you may
# not use this file except in compliance with the License. You may obtain
# a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
# WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
# License for the specific language governing permissions and limitations
# under the License.


# Compile Protocol Buffers definition files, remove redundant intermediary files and tag generated
# files.

# Setup - exit on any failure, record current dir and go to repo root.
set -e
current_dir=$(pwd)

IS_GIT=
if git rev-parse --git-dir > /dev/null 2>&1; then
    # This is a valid git repository

    cd "$(git rev-parse --show-toplevel)"
    IS_GIT=1
else
    # this is not a git repository

    script_dir="$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )"
    # assume this script is in third-party/java/protobuf
    cd "${script_dir}/../../.."
fi

# In a git repo we can use git ls-files which is faster and more
# accurate than a find
function ls_files_cmd {
    pattern="$1"
    if [ -n "$IS_GIT" ]; then
        git ls-files -c -o -- ${pattern}
    else
        find . -path "./${pattern}"
    fi
}

PROTOC="protoc"

# Compile all proto files visible to git (cached and others).
for f in $(ls_files_cmd src/**.proto); do
   $PROTOC --java_out=src-gen/ --java_opt=annotate_code $f
done

# Remove metadata files.
for f in $(ls_files_cmd src-gen/**/proto/*.meta); do
   rm $f
done

# Add generated tag to make tooling correctly recognize them as generated.
# Insert tag name into the command so that tooling does not mistaken this file with generated code.
tag_name="generated"
for f in $(ls_files_cmd src-gen/**/proto/*.java); do
  # Do not use sed - the only portable version of prepending a line is awful.
  temp_file=$(mktemp)
  echo "// @$tag_name" > $temp_file
  cat $f >> $temp_file
  mv $temp_file $f
done

cd "$current_dir"
set +e
