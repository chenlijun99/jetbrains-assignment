#!/usr/bin/env bash

function usage() {
	echo "Usage: $0 <is_proper_release> <plugin_zip_path>"
	echo ""
	echo "  <draft>: 'true' for a draft release for the current HEAD at main, 'false' for a proper release"
	echo "  <plugin_zip_path>: The path of the plugin zip file (e.g., './my-plugin-0.0.1.zip')."
	exit 1
}

if [ "$#" -ne 2 ]; then
	usage
fi

IS_DRAFT="$1"
PLUGIN_ZIP_PATH="$2"

VERSION=$(./gradlew properties --property version --quiet --console=plain | tail -n 1 | cut -f2- -d ' ')

RELEASE_TITLE=""
if [[ "$IS_DRAFT" == "true" ]]; then
	RELEASE_TITLE="Development build"
else
	RELEASE_TITLE="$VERSION"
fi

RELEASE_NOTE="./build/tmp/release_note.txt"
if [[ "$IS_DRAFT" == "true" ]]; then
	./gradlew getChangelog --unreleased --no-header --quiet --console=plain --output-file="$RELEASE_NOTE"
else
	./gradlew getChangelog --project-version "$VERSION" --no-header --quiet --console=plain --output-file="$RELEASE_NOTE"
fi

DRAFT_FLAG=""
if [[ "$IS_DRAFT" == "true" ]]; then
	DRAFT_FLAG="--draft"
fi

# https://github.com/orgs/community/discussions/24690#discussioncomment-3245075
gh release create v"$VERSION" \
	$DRAFT_FLAG \
	--title "$RELEASE_TITLE" \
	--notes-file "$RELEASE_NOTE" \
	"$PLUGIN_ZIP_PATH"
