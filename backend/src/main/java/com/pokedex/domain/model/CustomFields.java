package com.pokedex.domain.model;

import com.pokedex.domain.validation.Validator;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * Proprietary attributes that only exist in the local replica (User Story 03): a localized name,
 * geographical metadata, internal classification tags and free-form notes.
 * Instances are always normalized and valid.
 */
public record CustomFields(String localizedName, String region, Set<String> tags, String notes) {

    public static final int MAX_LOCALIZED_NAME = 100;
    public static final int MAX_REGION = 50;
    public static final int MAX_TAGS = 10;
    public static final int MAX_NOTES = 500;
    private static final Pattern TAG = Pattern.compile("^[a-z0-9][a-z0-9-]{0,29}$");

    public CustomFields {
        localizedName = blankToNull(localizedName);
        region = blankToNull(region);
        notes = blankToNull(notes);
        Set<String> normalizedTags = normalizeTags(tags);

        new Validator()
                .check(localizedName == null || localizedName.length() <= MAX_LOCALIZED_NAME,
                        "localizedName", "must be at most " + MAX_LOCALIZED_NAME + " characters")
                .check(region == null || region.length() <= MAX_REGION,
                        "region", "must be at most " + MAX_REGION + " characters")
                .check(normalizedTags.size() <= MAX_TAGS, "tags", "must contain at most " + MAX_TAGS + " tags")
                .check(normalizedTags.stream().allMatch(tag -> TAG.matcher(tag).matches()),
                        "tags", "each tag must be 1-30 lowercase letters, digits or hyphens")
                .check(notes == null || notes.length() <= MAX_NOTES,
                        "notes", "must be at most " + MAX_NOTES + " characters")
                .throwIfInvalid();

        // keeps insertion order so tags are returned the way the user entered them
        tags = Collections.unmodifiableSet(normalizedTags);
    }

    public static CustomFields empty() {
        return new CustomFields(null, null, Set.of(), null);
    }

    private static String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.strip();
    }

    private static Set<String> normalizeTags(Set<String> raw) {
        Set<String> normalized = new LinkedHashSet<>();
        if (raw != null) {
            raw.stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .map(tag -> tag.strip().toLowerCase(Locale.ROOT))
                    .forEach(normalized::add);
        }
        return normalized;
    }
}
