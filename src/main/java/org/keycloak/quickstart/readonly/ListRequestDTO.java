package org.keycloak.quickstart.readonly;

import lombok.Builder;

@Builder
public class ListRequestDTO {
    private final String filter;
    private final String fields;
    private final String omit;
    private final String sort;
    private final String q;
    private final Integer offset;
    private final Integer limit;
}
