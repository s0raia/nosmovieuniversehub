package com.nos.movieuniverse.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import java.util.ArrayList;
import java.util.List;
import java.math.BigDecimal;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * A local snapshot of a TMDB film. The TMDB id is the natural key, because the
 * catalogue holds several distinct films sharing a title.
 *
 * <p>A row whose {@code fetchedAt} is null is a stub: the importer creates it so
 * playlist and rating rows have something to reference before TMDB has been
 * called. Every other field is null on such a row.
 */
@Entity
public class Movie {

    @Id
    private Long tmdbId;

    private String title;

    private String originalTitle;

    private LocalDate releaseDate;

    private String overview;

    private String posterPath;

    private Integer runtime;

    /**
     * Null exactly when {@code tmdbVoteCount} is zero, never 0.0. TMDB sends 0.0
     * for an unvoted film and the database rejects that, so callers must
     * translate it. Precision 5 because a film with one perfect vote averages
     * 10.0, which does not fit in four digits.
     */
    @Column(precision = 5, scale = 3)
    private BigDecimal tmdbVoteAverage;

    @Column(nullable = false)
    private int tmdbVoteCount;

    private OffsetDateTime fetchedAt;

    @Column(length = 10)
    private String originalLanguage;

    /**
     * English names from TMDB {@code spoken_languages}. Null until enrichment has
     * run at least once after languages were added to the schema.
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private List<String> spokenLanguages;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "movie_genre",
            joinColumns = @JoinColumn(name = "tmdb_id"),
            inverseJoinColumns = @JoinColumn(name = "genre_id"))
    private Set<Genre> genres = new LinkedHashSet<>();

    protected Movie() {
        // required by JPA
    }

    /** Creates a stub row carrying nothing but the identifier. */
    public Movie(Long tmdbId) {
        this.tmdbId = tmdbId;
    }

    /** True while this row is a placeholder that TMDB has not filled in yet. */
    public boolean isStub() {
        return fetchedAt == null;
    }

    /** True when TMDB has no votes for this film, so no average may be shown. */
    public boolean hasNoVotes() {
        return tmdbVoteCount == 0;
    }

    public Long getTmdbId() {
        return tmdbId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getOriginalTitle() {
        return originalTitle;
    }

    public void setOriginalTitle(String originalTitle) {
        this.originalTitle = originalTitle;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public String getOverview() {
        return overview;
    }

    public void setOverview(String overview) {
        this.overview = overview;
    }

    public String getPosterPath() {
        return posterPath;
    }

    public void setPosterPath(String posterPath) {
        this.posterPath = posterPath;
    }

    public Integer getRuntime() {
        return runtime;
    }

    public void setRuntime(Integer runtime) {
        this.runtime = runtime;
    }

    public BigDecimal getTmdbVoteAverage() {
        return tmdbVoteAverage;
    }

    public int getTmdbVoteCount() {
        return tmdbVoteCount;
    }

    /**
     * Sets both halves of the vote pair together, since the database enforces
     * that the average is null if and only if the count is zero. Passing a count
     * of zero discards whatever average was supplied.
     */
    public void setVotes(int voteCount, BigDecimal voteAverage) {
        this.tmdbVoteCount = voteCount;
        this.tmdbVoteAverage = voteCount == 0 ? null : voteAverage;
    }

    public OffsetDateTime getFetchedAt() {
        return fetchedAt;
    }

    public void setFetchedAt(OffsetDateTime fetchedAt) {
        this.fetchedAt = fetchedAt;
    }

    public Set<Genre> getGenres() {
        return genres;
    }

    public String getOriginalLanguage() {
        return originalLanguage;
    }

    public void setOriginalLanguage(String originalLanguage) {
        this.originalLanguage = originalLanguage;
    }

    public List<String> getSpokenLanguages() {
        return spokenLanguages;
    }

    public void setSpokenLanguages(List<String> spokenLanguages) {
        this.spokenLanguages = spokenLanguages == null ? null : new ArrayList<>(spokenLanguages);
    }
}
