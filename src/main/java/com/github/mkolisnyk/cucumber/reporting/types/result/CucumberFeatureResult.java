package com.github.mkolisnyk.cucumber.reporting.types.result;

import org.apache.commons.lang.ArrayUtils;

import com.cedarsoftware.util.io.JsonObject;

public class CucumberFeatureResult {
    private String id;
    private CucumberTagResults[] tags;
    private String description;
    private String name;
    private String keyword;
    private Long line;
    private CucumberScenarioResult[] elements;
    private String uri;
    private float duration;

    @SuppressWarnings("unchecked")
    public CucumberFeatureResult(JsonObject<String, Object> json) {
        this.id = (String) json.get("id");
        JsonObject<String, Object> tagEntry = (JsonObject<String, Object>) json
                .get("tags");
        Object[] objs = {};
        if (tagEntry != null) {
            objs = (Object[]) ((JsonObject<String, Object>) json.get("tags"))
                    .get("@items");
        }
        this.tags = new CucumberTagResults[objs.length];
        for (int i = 0; i < objs.length; i++) {
            this.tags[i] = new CucumberTagResults(
                    (JsonObject<String, Object>) objs[i]);
        }
        this.description = (String) json.get("description");
        this.name = (String) json.get("name");
        this.keyword = (String) json.get("keyword");
        this.line = (Long) json.get("line");
        objs = (Object[]) ((JsonObject<String, Object>) json.get("elements"))
                .get("@items");
        this.elements = new CucumberScenarioResult[objs.length];
        for (int i = 0; i < objs.length; i++) {
            this.elements[i] = new CucumberScenarioResult(
                    (JsonObject<String, Object>) objs[i]);
        }
        this.uri = (String) json.get("uri");
    }

    private int passed = 0;
    private int failed = 0;
    private int undefined = 0;
    private int skipped = 0;

    public void valuate() {
        passed = 0;
        failed = 0;
        undefined = 0;
        duration = 0.f;
        for (CucumberScenarioResult scenario : elements) {
            scenario.valuate();
            if (scenario.getSteps() == null || scenario.getSteps().length <= 0) {
                this.undefined++;
            } else if (scenario.getFailed() > 0) {
                this.failed++;
            } else if (scenario.getUndefined() > 0) {
                this.undefined++;
            } else if (scenario.getSkipped() > 0) {
                this.skipped++;
            } else {
                this.passed++;
            }
            duration += scenario.getDuration();
        }
    }

    public String getStatus() {
        this.valuate();
        if (this.getFailed() > 0) {
            return "failed";
        } else if (this.getUndefined() > 0) {
            return "undefined";
        } else if (this.getSkipped() > 0) {
            return "skipped";
        } else {
            return "passed";
        }
    }

    /**
     * @return the passed
     */
    public final int getPassed() {
        return passed;
    }

    /**
     * @return the failed
     */
    public final int getFailed() {
        return failed;
    }

    /**
     * @return the undefined
     */
    public final int getUndefined() {
        return undefined;
    }

    public final int getSkipped() {
        return skipped;
    }

    /**
     * @return the id
     */
    public final String getId() {
        return id;
    }

    /**
     * @param idValue
     *            the id to set
     */
    public final void setId(String idValue) {
        this.id = idValue;
    }

    /**
     * @return the tags
     */
    public final CucumberTagResults[] getTags() {
        return tags;
    }

    /**
     * @param tagsValue
     *            the tags to set
     */
    public final void setTags(CucumberTagResults[] tagsValue) {
        this.tags = tagsValue;
    }

    /**
     * @return the description
     */
    public final String getDescription() {
        return description;
    }

    /**
     * @param descriptionValue
     *            the description to set
     */
    public final void setDescription(String descriptionValue) {
        this.description = descriptionValue;
    }

    /**
     * @return the name
     */
    public final String getName() {
        return name;
    }

    /**
     * @param nameValue
     *            the name to set
     */
    public final void setName(String nameValue) {
        this.name = nameValue;
    }

    /**
     * @return the keyword
     */
    public final String getKeyword() {
        return keyword;
    }

    /**
     * @param keywordValue
     *            the keyword to set
     */
    public final void setKeyword(String keywordValue) {
        this.keyword = keywordValue;
    }

    /**
     * @return the line
     */
    public final Long getLine() {
        return line;
    }

    /**
     * @param lineValue
     *            the line to set
     */
    public final void setLine(Long lineValue) {
        this.line = lineValue;
    }

    /**
     * @return the elements
     */
    public final CucumberScenarioResult[] getElements() {
        return elements;
    }

    /**
     * @param elementsValue
     *            the elements to set
     */
    public final void setElements(CucumberScenarioResult[] elementsValue) {
        this.elements = elementsValue;
    }

    /**
     * @return the uri
     */
    public final String getUri() {
        return uri;
    }

    /**
     * @param uriValue
     *            the uri to set
     */
    public final void setUri(String uriValue) {
        this.uri = uriValue;
    }

    /**
     * @return the duration
     */
    public final float getDuration() {
        return duration;
    }

    public void aggregateScenarioResults(boolean collapse) {
        String prevId = "";
        for (int i = 0; i < this.elements.length; i++) {
            if (this.elements[i].getKeyword().equalsIgnoreCase("Background")) {
                continue;
            }
            if (this.elements[i].getId().equals(prevId)) {
                this.elements[i].addRerunAttempts(this.elements[i - 1]
                        .getRerunAttempts() + 1);
                if (collapse) {
                    this.elements = (CucumberScenarioResult[]) ArrayUtils
                            .remove(this.elements, i - 1);
                    i--;
                    prevId = this.elements[i].getId();
                } else {
                    prevId = this.elements[i].getId();
                    this.elements[i].setId(String.format("%s-retry%d",
                            this.elements[i].getId(),
                            this.elements[i].getRerunAttempts()));
                    this.elements[i].setName(String.format("%s (retry %d)",
                            this.elements[i].getName(),
                            this.elements[i].getRerunAttempts()));
                }
            } else {
                prevId = this.elements[i].getId();
            }
        }
    }
}
