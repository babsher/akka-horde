#!/bin/bash
alias npm-exec='PATH=$(npm bin):$PATH'
cd src/main/resources/app
npm install
npm-exec bower install
npm-exec gulp build