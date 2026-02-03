package com.gaekdam.gaekdambe.dummy.generate.analytics_service.report.dashboard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.ReportTemplateType;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.domain.entity.ReportTemplate;
import com.gaekdam.gaekdambe.analytics_service.report.dashboard.command.infrastructure.repository.ReportTemplateRepository;

@Component
public class ReportTemplateGenerator {

    @Autowired
    private ReportTemplateRepository templateRepo;

    @Transactional
    public void generate() {
        // template_type 기준으로 이미 들어갔는지 체크하는게 더 안전 (count()는 다른 데이터가 있어도 막힘)
        for (ReportTemplateType type : ReportTemplateType.values()) {
            if (templateRepo.existsByTemplateType(type)) continue;

            ReportTemplate t = new ReportTemplate();
            t.setEmployeeCode(10000L);
            t.setTemplateType(type);

            switch (type) {
                case SUMMARY_ALL -> {
                    t.setTemplateName("전체 요약 템플릿");
                    t.setTemplateDesc("전체 항목을 요약해서 보여주는 대시보드 템플릿");
                }
                case SUMMARY_OPS -> {
                    t.setTemplateName("객실운영 요약 템플릿");
                    t.setTemplateDesc("객실 운영 관련 지표를 중심으로 보여주는 템플릿");
                }
                case SUMMARY_CUST -> {
                    t.setTemplateName("고객현황 요약 템플릿");
                    t.setTemplateDesc("고객 관련 지표 요약 템플릿");
                }
                case SUMMARY_CX -> {
                    t.setTemplateName("고객경험 요약 템플릿");
                    t.setTemplateDesc("문의/클레임 등 고객경험 지표 템플릿");
                }
                case SUMMARY_REV -> {
                    t.setTemplateName("예약및매출 요약 템플릿");
                    t.setTemplateDesc("예약/매출 관련 주요 지표 템플릿");
                }
            }

            t.setIsActive(true);
            t.setVersion(1);

            templateRepo.save(t); // templateId는 DB가 자동 생성
        }
    }
}
