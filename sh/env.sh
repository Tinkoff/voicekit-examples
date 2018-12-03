#! /usr/bin/env bash

if [ ! -d "sh" ]; then
    echo "Directory ./sh does not exist. Are you running from repo root?"; return -1
fi

if [ ! -d "python" ]; then
    echo "Directory ./python does not exist. Are you running from repo root?"; return -1
fi

export PYTHONPATH=$(pwd)/python
echo "Adding $PYTHONPATH to PYTHONPATH"
