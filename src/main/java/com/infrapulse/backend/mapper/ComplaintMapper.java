package com.infrapulse.backend.mapper;

import com.infrapulse.backend.dto.complaint.*;
import com.infrapulse.backend.entity.Comment;
import com.infrapulse.backend.entity.Complaint;
import com.infrapulse.backend.entity.ComplaintStatusHistory;

import java.util.List;

public class ComplaintMapper {

    private ComplaintMapper() {}

    public static ComplaintSummaryResponse toSummary(Complaint c, boolean isBookmarked) {
        return new ComplaintSummaryResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getCategory(),
                c.getStatus(),
                c.getPriority(),
                c.getLatitude(),
                c.getLongitude(),
                c.getAddress(),
                c.getImageUrl(),
                c.getComments() != null ? c.getComments().size() : 0,
                c.getRating() != null ? c.getRating().getValue() : null,
                isBookmarked,
                c.getCreatedAt()
        );
    }

    public static ComplaintDetailResponse toDetail(Complaint c, boolean isBookmarked, Integer myRating) {
        List<StatusHistoryResponse> history = c.getStatusHistory() == null ? List.of() :
                c.getStatusHistory().stream()
                        .sorted((a, b) -> a.getTimestamp().compareTo(b.getTimestamp()))
                        .map(ComplaintMapper::toHistoryResponse)
                        .toList();

        List<CommentResponse> comments = c.getComments() == null ? List.of() :
                c.getComments().stream()
                        .sorted((a, b) -> a.getCreatedAt().compareTo(b.getCreatedAt()))
                        .map(ComplaintMapper::toCommentResponse)
                        .toList();

        return new ComplaintDetailResponse(
                c.getId(),
                c.getTitle(),
                c.getDescription(),
                c.getCategory(),
                c.getStatus(),
                c.getPriority(),
                c.getLatitude(),
                c.getLongitude(),
                c.getAddress(),
                c.getImageUrl(),
                c.getCitizen().getId(),
                c.getCitizen().getName(),
                c.getOfficer() != null ? c.getOfficer().getId() : null,
                c.getOfficer() != null ? c.getOfficer().getName() : null,
                isBookmarked,
                myRating,
                history,
                comments,
                c.getCreatedAt(),
                c.getResolvedAt()
        );
    }

    public static StatusHistoryResponse toHistoryResponse(ComplaintStatusHistory h) {
        return new StatusHistoryResponse(h.getStatus(), h.getNote(), h.getPhotoUrl(), h.getTimestamp());
    }

    public static CommentResponse toCommentResponse(Comment c) {
        return new CommentResponse(c.getId(), c.getAuthor().getName(), c.getAuthor().getId(), c.getMessage(), c.getCreatedAt());
    }

    public static HeatmapPoint toHeatmapPoint(Complaint c) {
        return new HeatmapPoint(c.getId(), c.getTitle(), c.getAddress(), c.getStatus(), c.getCategory(), c.getLatitude(), c.getLongitude());
    }
}
