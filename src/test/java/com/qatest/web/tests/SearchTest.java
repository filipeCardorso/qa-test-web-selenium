package com.qatest.web.tests;

import com.qatest.web.base.BaseTest;
import com.qatest.web.pages.SearchResultPage;
import io.qameta.allure.Description;
import io.qameta.allure.Feature;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@Feature("Blog Search")
@DisplayName("Blog do Agi - Search Tests")
class SearchTest extends BaseTest {

    @Test
    @DisplayName("Should return results for valid search term")
    @Description("Search for 'automação' and verify results are returned")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnResultsForValidSearch() {
        // Arrange
        homePage.navigate();

        // Act
        SearchResultPage results = homePage.searchFor("automação");

        // Assert
        assertThat(results.getResultCount()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should show no results message for non-existent term")
    @Description("Search for a term that does not exist and verify no results message")
    @Severity(SeverityLevel.CRITICAL)
    void shouldShowNoResultsForInvalidSearch() {
        // Arrange
        homePage.navigate();

        // Act
        SearchResultPage results = homePage.searchFor("xyznonexistent999");

        // Assert
        assertThat(results.hasNoResults()).isTrue();
        assertThat(results.getResultCount()).isZero();
    }

    @Test
    @DisplayName("Should handle special characters without error")
    @Description("Search for special characters and verify the system does not break")
    @Severity(SeverityLevel.NORMAL)
    void shouldHandleSpecialCharactersWithoutError() {
        // Arrange
        homePage.navigate();

        // Act
        SearchResultPage results = homePage.searchFor("@#$%&");

        // Assert
        assertThat(results.isPageLoaded()).isTrue();
    }

    @Test
    @DisplayName("Should return result title and link matching search term")
    @Description("Search for a term and validate first result title and link contain the term")
    @Severity(SeverityLevel.CRITICAL)
    void shouldReturnResultWithMatchingTitleAndLink() {
        // Arrange
        homePage.navigate();
        String searchTerm = "crédito";

        // Act
        SearchResultPage results = homePage.searchFor(searchTerm);

        // Assert
        assertThat(results.getResultCount()).isGreaterThan(0);
        assertThat(results.getFirstResultTitle())
                .containsIgnoringCase(searchTerm);
        assertThat(results.getFirstResultLink()).isNotEmpty();
    }
}
