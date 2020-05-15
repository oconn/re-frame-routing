#!/usr/bin/env bash

if [[ $(git diff --stat) != '' ]]; then
  echo "Error: dirty repo"
  exit 1
elif [[ $(git rev-parse --show-toplevel) != $(pwd) ]]; then
  echo "Error: Not in root directory"
  exit 1
elif [ -z "$CLOJARS_USERNAME" ]; then
  echo "Error: CLOJARS_USERNAME is empty"
  exit 1
elif [ -z "$CLOJARS_PASSWORD" ]; then
  echo "Error: CLOJARS_PASSWORD is empty"
  exit 1
fi

# Generate pom file
clojure -Spom

perl -pi -e "s/HEAD/$(git rev-parse HEAD)/g" pom.xml

# Generate jar file
rm -f app.jar
clojure -Apack

# Upload to clojars
clj -Adeploy

perl -pi -e "s/$(git rev-parse HEAD)/HEAD/g" pom.xml

# Format pom file
xmllint --format pom.xml > pom.xml-formatted
mv pom.xml-formatted pom.xml
