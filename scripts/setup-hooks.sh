#!/bin/bash

git config core.hooksPath .githooks

chmod +x .githooks/branch-commit-naming-restriction

echo "Git hooks installed"