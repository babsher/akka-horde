#!/bin/bash
cd src/main/resources/app
npm install
PATH=$(npm bin):$PATH bower install
PATH=$(npm bin):$PATH gulp build