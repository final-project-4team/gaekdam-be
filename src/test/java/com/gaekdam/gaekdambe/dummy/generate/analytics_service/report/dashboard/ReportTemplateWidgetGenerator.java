package com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dashboard;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.ReportTemplateType;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplateWidget;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateWidgetRepository;

@Component
public class ReportTemplateWidgetGenerator {

    private final ReportTemplateRepository templateRepo;
    private final ReportTemplateWidgetRepository widgetRepo;

    public ReportTemplateWidgetGenerator(
            ReportTemplateRepository templateRepo,
            ReportTemplateWidgetRepository widgetRepo
    ) {
        this.templateRepo = templateRepo;
        this.widgetRepo = widgetRepo;
    }

    @Transactional
    public void generate() {

        for (ReportTemplateType type : ReportTemplateType.values()) {

            ReportTemplate template = templateRepo.findByTemplateType(type)
                    .orElseThrow(() -> new IllegalStateException("ReportTemplate not found for templateType=" + type));

            Long templateId = template.getTemplateId();

            // templateId 기준으로 이미 위젯이 들어갔다면 스킵
            if (widgetRepo.existsByTemplateId(templateId)) {
                System.out.println("[SKIP] widgets already exist. templateType=" + type + ", templateId=" + templateId);
                continue;
            }

            List<ReportTemplateWidget> widgets = buildWidgets(type, templateId);

            widgetRepo.saveAll(widgets);
            System.out.println("[OK] seeded widgets. templateType=" + type + ", templateId=" + templateId + ", count=" + widgets.size());
        }
    }

    private List<ReportTemplateWidget> buildWidgets(ReportTemplateType type, Long templateId) {
        List<ReportTemplateWidget> list = new ArrayList<>();
        int order = 1;

        switch (type) {

            // 1) 전체 요약 (KPI 카드 16개)
            case SUMMARY_ALL -> {
                order = addKpiCard(list, templateId, order, "체크인", "OPS", "CHECKIN_COUNT");
                order = addKpiCard(list, templateId, order, "체크아웃", "OPS", "CHECKOUT_COUNT");
                order = addKpiCard(list, templateId, order, "평균객실단가", "OPS", "ADR");
                order = addKpiCard(list, templateId, order, "객실점유율", "OPS", "OCCUPANCY_RATE");

                order = addKpiCard(list, templateId, order, "투숙객", "CUST", "GUEST_COUNT");
                order = addKpiCard(list, templateId, order, "재방문율", "CUST", "REPEAT_CUSTOMER_RATE");
                order = addKpiCard(list, templateId, order, "멤버십 비율", "CUST", "MEMBERSHIP_RATE");
                order = addKpiCard(list, templateId, order, "외국인 비율", "CUST", "FOREIGN_CUSTOMER_RATE");

                order = addKpiCard(list, templateId, order, "고객 문의", "CX", "TOTAL_INQUIRY_COUNT");
                order = addKpiCard(list, templateId, order, "고객 클레임", "CX", "CLAIM_COUNT");
                order = addKpiCard(list, templateId, order, "미처리 문의 비율", "CX", "UNRESOLVED_RATE");
                order = addKpiCard(list, templateId, order, "평균응답시간", "CX", "AVG_RESPONSE_TIME");

                order = addKpiCard(list, templateId, order, "예약", "REV", "RESERVATION_COUNT");
                order = addKpiCard(list, templateId, order, "예약 취소율", "REV", "CANCEL_RATE");
                order = addKpiCard(list, templateId, order, "노쇼율", "REV", "NO_SHOW_RATE");
                order = addKpiCard(list, templateId, order, "객실 외 매출비율", "REV", "NON_ROOM_REVENUE_RATIO");
            }

            // 2) 객실운영 요약: KPI 4 + 라인 4
            case SUMMARY_OPS -> {
                order = addKpiCard(list, templateId, order, "체크인", "OPS", "CHECKIN_COUNT");
                order = addKpiCard(list, templateId, order, "체크아웃", "OPS", "CHECKOUT_COUNT");
                order = addKpiCard(list, templateId, order, "평균객실단가", "OPS", "ADR");
                order = addKpiCard(list, templateId, order, "객실점유율", "OPS", "OCCUPANCY_RATE");

                order = addLine(list, templateId, order, "월간/연간 체크인 변화량", "OPS", "CHECKIN_COUNT");
                order = addLine(list, templateId, order, "월간/연간 체크아웃 변화량", "OPS", "CHECKOUT_COUNT");
                order = addLine(list, templateId, order, "월간/연간 평균객실단가 변화량", "OPS", "ADR");
                order = addLine(list, templateId, order, "월간/연간 객실점유율 변화량", "OPS", "OCCUPANCY_RATE");
            }

            // 3) 고객현황 요약: KPI 4 + 도넛/게이지 + Top3 막대
            case SUMMARY_CUST -> {
                order = addKpiCard(list, templateId, order, "투숙객", "CUST", "GUEST_COUNT");
                order = addKpiCard(list, templateId, order, "재방문율", "CUST", "REPEAT_CUSTOMER_RATE");
                order = addKpiCard(list, templateId, order, "멤버십 비율", "CUST", "MEMBERSHIP_RATE");
                order = addKpiCard(list, templateId, order, "외국인 비율", "CUST", "FOREIGN_CUSTOMER_RATE");

                // 고객유형 비율(개인/법인/단체) - 도넛/파이
                order = addPie(list, templateId, order, "고객유형 비율", "CUST", "CUSTOMER_TYPE_RATIO", "CONTRACT_TYPE");

                // 외국인 고객 Top3 국가 - bar
                order = addBar(list, templateId, order, "외국인 고객 Top3 국가", "CUST", "FOREIGN_TOP_COUNTRY", "COUNTRY_CODE");
            }

            // 4) 고객경험 요약: KPI 4 + 라인 2
            case SUMMARY_CX -> {
                order = addKpiCard(list, templateId, order, "고객 문의", "CX", "TOTAL_INQUIRY_COUNT");
                order = addKpiCard(list, templateId, order, "고객 클레임", "CX", "CLAIM_COUNT");
                order = addKpiCard(list, templateId, order, "미처리 문의 비율", "CX", "UNRESOLVED_RATE");
                order = addKpiCard(list, templateId, order, "평균응답시간", "CX", "AVG_RESPONSE_TIME");

                order = addLine(list, templateId, order, "월간/연간 고객 문의 변화량", "CX", "TOTAL_INQUIRY_COUNT");
                order = addLine(list, templateId, order, "월간/연간 고객 클레임 변화량", "CX", "CLAIM_COUNT");
            }

            // 5) 예약및매출 요약: KPI 4 + 라인 4
            case SUMMARY_REV -> {
                order = addKpiCard(list, templateId, order, "예약", "REV", "RESERVATION_COUNT");
                order = addKpiCard(list, templateId, order, "예약 취소율", "REV", "CANCEL_RATE");
                order = addKpiCard(list, templateId, order, "노쇼율", "REV", "NO_SHOW_RATE");
                order = addKpiCard(list, templateId, order, "부대시설 매출비율", "REV", "FACILITY_REVENUE_RATIO");

                order = addLine(list, templateId, order, "월간/연간 예약 수 변화량", "REV", "RESERVATION_COUNT");
                order = addLine(list, templateId, order, "월간/연간 예약 취소 수 변화량", "REV", "CANCEL_COUNT");
                order = addLine(list, templateId, order, "월간/연간 노쇼 수 변화량", "REV", "NO_SHOW_COUNT");
                order = addLine(list, templateId, order, "월간/연간 부대시설 매출 변화량", "REV", "FACILITY_REVENUE");
            }
        }

        return list;
    }

    private int addKpiCard(List<ReportTemplateWidget> list, Long templateId, int order,
                           String title, String datasetType, String metricKey) {
        list.add(widget(templateId, "KPI_CARD", title, datasetType, metricKey, null, order));
        return order + 1;
    }

    private int addLine(List<ReportTemplateWidget> list, Long templateId, int order,
                        String title, String datasetType, String metricKey) {
        list.add(widget(templateId, "LINE", title, datasetType, metricKey, null, order));
        return order + 1;
    }

    private int addBar(List<ReportTemplateWidget> list, Long templateId, int order,
                       String title, String datasetType, String metricKey, String dimensionKey) {
        list.add(widget(templateId, "BAR", title, datasetType, metricKey, dimensionKey, order));
        return order + 1;
    }

    private int addPie(List<ReportTemplateWidget> list, Long templateId, int order,
                       String title, String datasetType, String metricKey, String dimensionKey) {
        // 너희 widget_type 정의에 PIE가 없다면 TABLE/BAR/GAUGE 중 하나로 바꾸거나 enum에 추가
        // 지금은 화면이 도넛이라 "GAUGE"로 임시 처리 (프론트에서 도넛 렌더링으로 사용 가능)
        list.add(widget(templateId, "GAUGE", title, datasetType, metricKey, dimensionKey, order));
        return order + 1;
    }

    private ReportTemplateWidget widget(Long templateId, String widgetType, String title,
                                        String datasetType, String metricKey, String dimensionKey, int sortOrder) {
        ReportTemplateWidget w = new ReportTemplateWidget();
        w.setTemplateId(templateId);
        w.setWidgetType(widgetType);
        w.setTitle(title);
        w.setDatasetType(datasetType);
        w.setMetricKey(metricKey);
        w.setDimensionKey(dimensionKey);
        w.setDefaultPeriod("MONTH");
        w.setDefaultSortOrder(sortOrder);

        // 필요시 옵션/필터 JSON도 시드 가능
        w.setOptionsJson(null);
        w.setDefaultFilterJson(null);
        return w;
    }
}
