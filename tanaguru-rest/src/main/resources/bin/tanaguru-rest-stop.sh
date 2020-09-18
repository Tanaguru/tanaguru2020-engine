#!/bin/bash
cd "$(dirname "$0")"
kill $(cat pid.txt)
