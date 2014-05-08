package com.zjhcsoft.rule.config.controller;

import com.zjhcsoft.qin.exchange.controller.ControllerHelper;
import com.zjhcsoft.qin.exchange.dto.ResponseVO;
import com.zjhcsoft.rule.common.RuleConstants.Type;
import com.zjhcsoft.rule.common.controller.BaseController;
import com.zjhcsoft.rule.common.util.LatnIdHelper;
import com.zjhcsoft.rule.config.entity.RuleGroup;
import com.zjhcsoft.rule.config.entity.RuleKpiDefine;
import com.zjhcsoft.rule.config.service.RuleGroupService;
import com.zjhcsoft.rule.config.service.RuleGroupTaskService;
import com.zjhcsoft.rule.config.service.RuleKpiDefineService;
import com.zjhcsoft.rule.config.service.RuleRelationService;
import com.zjhcsoft.rule.config.vo.RuleKpiLogVo;
import com.zjhcsoft.rule.log.entity.RuleKpiProcessLog;
import com.zjhcsoft.rule.log.service.RuleKpiProcessLogService;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA.
 * User: XuanLubin  Date: 14-3-12  Time: 下午3:58
 */
@RestController
@RequestMapping("config/group")
public class RuleGroupController extends BaseController<RuleGroupService, RuleGroup> {

    @Inject
    private RuleKpiDefineService ruleKpiDefineService;

    @Inject
    private RuleRelationService ruleRelationService;

    @Inject
    private RuleKpiProcessLogService logService;

    @RequestMapping("{s}/status")
    public ResponseVO findByStatus(@PathVariable int s, HttpServletRequest request) {
        return ControllerHelper.success(service.findAllByStatus(s, latnIdHelper.getLatnId(request.getSession())));
    }

    @RequestMapping("")
    public ResponseVO findAll(HttpServletRequest request) {
        return ControllerHelper.success(service.findAll(latnIdHelper.getLatnId(request.getSession())));
    }

    @RequestMapping("{groupId}/kpiList")
    public ResponseVO groupKpiList(@PathVariable Long groupId, HttpServletRequest httpServletRequest) {
        int page[] = ControllerHelper.getPageParameters(httpServletRequest);
        if (null != page) {
            com.ecfront.easybi.dbutils.exchange.Page pageKpi = ruleRelationService.findIdBy(page[0], page[1], groupId, new String[]{Type.RULE_GROUP_BASE_RULE_KPI, Type.RULE_GROUP_MIX_RULE_KPI});
            pageKpi.objects = packageLog(ruleKpiDefineService.findByIdsWithSort(pageKpi.objects));
            return ControllerHelper.success(pageKpi);
        } else {
            List<Long> kpiIds = ruleRelationService.queryIdByFromIdRelType(groupId, new String[]{Type.RULE_GROUP_BASE_RULE_KPI, Type.RULE_GROUP_MIX_RULE_KPI});
            return ControllerHelper.success(packageLog(ruleKpiDefineService.findByIdsWithSort(kpiIds)));
        }
    }

    @RequestMapping("check/{groupId}")
    public ResponseVO checkDeleteAble(@PathVariable Long groupId) {
        if (null != groupId) {
            List<Long> kpiIds = ruleRelationService.queryIdByFromIdRelType(groupId, new String[]{Type.RULE_GROUP_BASE_RULE_KPI, Type.RULE_GROUP_MIX_RULE_KPI});
            return ControllerHelper.success(kpiIds.size() < 1);
        }
        return ControllerHelper.success(true);
    }

    private List<RuleKpiLogVo> packageLog(List<RuleKpiDefine> kpiDefineList) {
        List<RuleKpiLogVo> logVoList = new ArrayList<>();
        for (RuleKpiDefine define : kpiDefineList) {
            logVoList.add(new RuleKpiLogVo(define, logService.findLatestLog(define.getKpiCode())));
        }
        return logVoList;
    }

    @Inject
    private RuleKpiDefineController kpiDefineController;

    @Inject
    private RuleGroupTaskService groupTaskService;

    @Override
    public ResponseVO delete(@PathVariable String pk) {
        if (StringUtils.isNumeric(pk)) {
            Long groupId = Long.parseLong(pk);
            List<Long> kpiList = ruleRelationService.queryIdByFromIdRelType(groupId, new String[]{Type.RULE_GROUP_MIX_RULE_KPI, Type.RULE_GROUP_BASE_RULE_KPI});

            //删除规则关联
            ruleRelationService.deleteAllByFromId(groupId, new String[]{Type.RULE_GROUP_MIX_RULE_KPI, Type.RULE_GROUP_BASE_RULE_KPI});

            //删除规则
            for (Long id : kpiList) {
                kpiDefineController.delete(String.valueOf(id));
            }
            //删除任务实例
            groupTaskService.deleteByRuleGroupRowId(groupId);

            //删除任务
            return super.delete(pk);
        } else {
            return ControllerHelper.success("参数");
        }
    }
}