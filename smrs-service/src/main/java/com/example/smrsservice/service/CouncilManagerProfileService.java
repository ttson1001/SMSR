//package com.example.smrsservice.service;
//
//import com.example.smrsservice.common.CouncilManagerStatus;
//import com.example.smrsservice.entity.Account;
//import com.example.smrsservice.entity.CouncilManagerProfile;
//import com.example.smrsservice.repository.AccountRepository;
//import com.example.smrsservice.repository.CouncilManagerProfileRepository;
//import lombok.RequiredArgsConstructor;
//import org.apache.poi.ss.usermodel.*;
//import org.apache.poi.xssf.usermodel.XSSFWorkbook;
//import org.springframework.security.core.Authentication;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.util.StringUtils;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.InputStream;
//import java.time.LocalDate;
//import java.util.*;
//
//@Service
//@RequiredArgsConstructor
//@Transactional
//public class CouncilManagerProfileService {
//    private final CouncilManagerProfileRepository councilManagerProfileRepository;
//    private final AccountRepository accountRepository;
//
//    /**
//     * Import Council Manager Profiles từ file Excel
//     *
//     * Excel format (header row - không phân biệt hoa thường):
//     * - email: Email của account (bắt buộc)
//     * - employee_code: Mã cán bộ (bắt buộc)
//     * - council_name: Tên hội đồng
//     * - council_code: Mã hội đồng (để phân biệt các hội đồng khác nhau)
//     * - department: Khoa/Phòng/Ban
//     * - position_title: Chức danh (Chủ tịch, Ủy viên, Thư ký...)
//     * - status: Trạng thái (ACTIVE/INACTIVE/SUSPENDED)
//     * - start_date: Ngày bắt đầu
//     * - end_date: Ngày kết thúc
//     * - note: Ghi chú
//     */
//    @Transactional
//    public List<CouncilManagerProfile> importCouncilManagerProfilesFromExcel(MultipartFile file) {
//        List<CouncilManagerProfile> profiles = new ArrayList<>();
//
//        try (InputStream is = file.getInputStream(); Workbook workbook = new XSSFWorkbook(is)) {
//            Sheet sheet = workbook.getSheetAt(0);
//            Iterator<Row> rows = sheet.iterator();
//
//            // ========== ĐỌC HEADER ROW ==========
//            Map<String, Integer> headerMap = new HashMap<>();
//            if (rows.hasNext()) {
//                Row headerRow = rows.next();
//                for (Cell cell : headerRow) {
//                    String columnName = cell.getStringCellValue().trim().toLowerCase();
//                    headerMap.put(columnName, cell.getColumnIndex());
//                }
//            }
//
//            // Kiểm tra các cột bắt buộc
//            if (!headerMap.containsKey("email")) {
//                throw new RuntimeException("Missing required column: email");
//            }
//            if (!headerMap.containsKey("employee_code")) {
//                throw new RuntimeException("Missing required column: employee_code");
//            }
//
//            // ========== ĐỌC DATA ROWS ==========
//            int rowNumber = 1;
//            while (rows.hasNext()) {
//                Row row = rows.next();
//                rowNumber++;
//
//                final int currentRow = rowNumber;
//
//                try {
//                    // ========== EMAIL (bắt buộc) ==========
//                    String email = getCellValue(row.getCell(headerMap.get("email")));
//                    if (email == null || email.isBlank()) {
//                        System.out.println("Row " + currentRow + ": Email is empty, skipping...");
//                        continue;
//                    }
//
//                    // Tìm account theo email
//                    Account account = accountRepository.findByEmail(email)
//                            .orElseThrow(() -> new RuntimeException(
//                                    "Row " + currentRow + ": Account not found with email: " + email));
//
//                    // ========== EMPLOYEE CODE (bắt buộc) ==========
//                    String employeeCode = getCellValue(row.getCell(headerMap.get("employee_code")));
//                    if (employeeCode.isBlank()) {
//                        System.out.println("Row " + currentRow + ": Employee code is empty, skipping...");
//                        continue;
//                    }
//
//                    // ========== COUNCIL CODE ==========
//                    String councilCode = null;
//                    if (headerMap.containsKey("council_code")) {
//                        councilCode = getCellValue(row.getCell(headerMap.get("council_code")));
//                    }
//
//                    // Tìm hoặc tạo mới CouncilManagerProfile
//                    CouncilManagerProfile profile;
//                    if (councilCode != null && !councilCode.isBlank()) {
//                        // Nếu có council code, tìm theo account + council code
//                        profile = councilManagerProfileRepository
//                                .findByAccountIdAndCouncilCode(account.getId(), councilCode)
//                                .orElse(new CouncilManagerProfile());
//                    } else {
//                        // Nếu không có council code, tạo mới
//                        profile = new CouncilManagerProfile();
//                    }
//
//                    profile.setAccount(account);
//                    profile.setEmployeeCode(employeeCode);
//
//                    // ========== COUNCIL NAME ==========
//                    if (headerMap.containsKey("council_name")) {
//                        profile.setCouncilName(getCellValue(row.getCell(headerMap.get("council_name"))));
//                    }
//
//                    // ========== COUNCIL CODE ==========
//                    if (councilCode != null && !councilCode.isBlank()) {
//                        profile.setCouncilCode(councilCode);
//                    }
//
//                    // ========== DEPARTMENT ==========
//                    if (headerMap.containsKey("department")) {
//                        profile.setDepartment(getCellValue(row.getCell(headerMap.get("department"))));
//                    }
//
//                    // ========== POSITION TITLE ==========
//                    if (headerMap.containsKey("position_title")) {
//                        profile.setPositionTitle(getCellValue(row.getCell(headerMap.get("position_title"))));
//                    }
//
//                    // ========== STATUS ==========
//                    if (headerMap.containsKey("status")) {
//                        String statusValue = getCellValue(row.getCell(headerMap.get("status"))).toUpperCase();
//                        try {
//                            profile.setStatus(CouncilManagerStatus.valueOf(statusValue));
//                        } catch (IllegalArgumentException e) {
//                            profile.setStatus(CouncilManagerStatus.ACTIVE);
//                        }
//                    } else {
//                        profile.setStatus(CouncilManagerStatus.ACTIVE);
//                    }
//
//                    // ========== START DATE ==========
//                    if (headerMap.containsKey("start_date")) {
//                        LocalDate startDate = getDateCellValue(row.getCell(headerMap.get("start_date")));
//                        if (startDate != null) {
//                            profile.setStartDate(startDate);
//                        }
//                    }
//
//                    // ========== END DATE ==========
//                    if (headerMap.containsKey("end_date")) {
//                        LocalDate endDate = getDateCellValue(row.getCell(headerMap.get("end_date")));
//                        if (endDate != null) {
//                            profile.setEndDate(endDate);
//                        }
//                    }
//
//                    // ========== NOTE ==========
//                    if (headerMap.containsKey("note")) {
//                        profile.setNote(getCellValue(row.getCell(headerMap.get("note"))));
//                    }
//
//                    profiles.add(profile);
//                    System.out.println("Row " + rowNumber + ": Processed council member " + email +
//                            (councilCode != null ? " (Council: " + councilCode + ")" : ""));
//
//                } catch (Exception e) {
//                    System.err.println("Row " + rowNumber + " - Error: " + e.getMessage());
//                    // Tiếp tục xử lý row tiếp theo
//                }
//            }
//
//            councilManagerProfileRepository.saveAll(profiles);
//
//        } catch (Exception e) {
//            throw new RuntimeException("Lỗi khi đọc file Excel: " + e.getMessage(), e);
//        }
//
//        return profiles;
//    }
//
//    /**
//     * Lấy giá trị String từ cell
//     */
//    private String getCellValue(Cell cell) {
//        if (cell == null) return "";
//
//        switch (cell.getCellType()) {
//            case STRING:
//                return cell.getStringCellValue().trim();
//            case NUMERIC:
//                if (DateUtil.isCellDateFormatted(cell)) {
//                    return cell.getLocalDateTimeCellValue().toLocalDate().toString();
//                }
//                return String.valueOf((long) cell.getNumericCellValue());
//            case BOOLEAN:
//                return String.valueOf(cell.getBooleanCellValue());
//            case FORMULA:
//                return cell.getCellFormula();
//            case BLANK:
//                return "";
//            default:
//                return "";
//        }
//    }
//
//    /**
//     * Lấy giá trị Date từ cell
//     */
//    private LocalDate getDateCellValue(Cell cell) {
//        if (cell == null) return null;
//
//        try {
//            if (cell.getCellType() == CellType.NUMERIC && DateUtil.isCellDateFormatted(cell)) {
//                return cell.getLocalDateTimeCellValue().toLocalDate();
//            } else if (cell.getCellType() == CellType.STRING) {
//                String dateStr = cell.getStringCellValue().trim();
//                if (dateStr.isBlank()) return null;
//
//                // Thử parse theo format yyyy-MM-dd
//                return LocalDate.parse(dateStr);
//            }
//        } catch (Exception e) {
//            System.err.println("Cannot parse date from cell: " + e.getMessage());
//        }
//
//        return null;
//    }
//}
