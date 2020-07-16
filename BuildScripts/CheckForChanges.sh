#!/bin/bash
if [[ -n $(git status -s .) ]]; then
  echo "##vso[task.logissue type=error;] $1"
  git status
  exit 1
fi
