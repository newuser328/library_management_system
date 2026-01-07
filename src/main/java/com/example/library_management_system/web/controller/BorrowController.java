package com.example.library_management_system.web.controller;

import com.example.library_management_system.domain.entity.Borrow;
import com.example.library_management_system.domain.enums.BorrowStatus;
import com.example.library_management_system.service.BorrowService;
import com.example.library_management_system.web.dto.ApiResponse;
import com.example.library_management_system.web.dto.BorrowCreateRequest;
import com.example.library_management_system.web.dto.BorrowDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/borrows")
@RequiredArgsConstructor
public class BorrowController {

    private final BorrowService borrowService;

    /**
     * 读者发起借阅申请（从登录态获取当前用户）
     */
    @PostMapping
    public ApiResponse<BorrowDto> request(Authentication authentication,
                                         @Valid @RequestBody BorrowCreateRequest req) {
        Borrow borrow = borrowService.requestBorrow(req.getBookId(), req.getRemark());
        return ApiResponse.ok(toDto(borrow));
    }

    /**
     * 管理员审核通过
     */
    @PostMapping("/{id}/approve")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BorrowDto> approve(@PathVariable Long id) {
        return ApiResponse.ok(toDto(borrowService.approve(id)));
    }

    /**
     * 管理员拒绝
     */
    @PostMapping("/{id}/reject")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BorrowDto> reject(@PathVariable Long id, @RequestParam(required = false) String remark) {
        return ApiResponse.ok(toDto(borrowService.reject(id, remark)));
    }

    /**
     * 归还登记
     */
    @PostMapping("/{id}/return")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<BorrowDto> markReturned(@PathVariable Long id) {
        return ApiResponse.ok(toDto(borrowService.markReturned(id)));
    }

    /**
     * 管理员查看借阅记录（可按状态/拼音首字母筛选）
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Page<BorrowDto>> list(
            @RequestParam(required = false) BorrowStatus status,
            @RequestParam(required = false) String titleInitial,
            Pageable pageable
    ) {
        return ApiResponse.ok(borrowService.listAll(status, titleInitial, pageable).map(this::toDto));
    }

    /**
     * 读者查看我的借阅
     */
    @GetMapping("/my")
    public ApiResponse<Page<BorrowDto>> my(Pageable pageable) {
        return ApiResponse.ok(borrowService.listMyBorrows(pageable).map(this::toDto));
    }

    private BorrowDto toDto(Borrow b) {
        return BorrowDto.builder()
                .id(b.getId())
                .status(b.getStatus())
                .requestTime(b.getRequestTime())
                .approveTime(b.getApproveTime())
                .rejectTime(b.getRejectTime())
                .returnTime(b.getReturnTime())
                .dueDate(b.getDueDate())
                .remark(b.getRemark())
                .userId(b.getUser().getId())
                .username(b.getUser().getUsername())
                .bookId(b.getBook().getId())
                .bookTitle(b.getBook().getTitle())
                .bookIsbn(b.getBook().getIsbn())
                .build();
    }
}
