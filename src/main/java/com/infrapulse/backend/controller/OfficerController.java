package com.infrapulse.backend.controller;

import com.infrapulse.backend.dto.user.OfficerResponse;
import com.infrapulse.backend.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/officers")
@RequiredArgsConstructor
public class OfficerController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<OfficerResponse>> getOfficers(@RequestParam(required = false) Long departmentId) {
        return ResponseEntity.ok(userService.getOfficers(departmentId));
    }

    @GetMapping("/leaderboard")
    public ResponseEntity<List<OfficerResponse>> getLeaderboard() {
        return ResponseEntity.ok(userService.getLeaderboard());
    }
}
