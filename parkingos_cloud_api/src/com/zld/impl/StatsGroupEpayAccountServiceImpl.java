package com.zld.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.zld.pojo.StatsAccount;
import com.zld.pojo.StatsAccountResp;
import com.zld.pojo.StatsReq;
import com.zld.service.PgOnlyReadService;
import com.zld.service.StatsAccountService;
@Service("groupEpay")
public class StatsGroupEpayAccountServiceImpl implements StatsAccountService {
	@Autowired
	private PgOnlyReadService readService;
	
	Logger logger = Logger.getLogger(StatsGroupEpayAccountServiceImpl.class);
	@Override
	public StatsAccountResp statsAccount(StatsReq req) {
		//logger.error(req.toString());
		StatsAccountResp resp = new StatsAccountResp();
		try {
			long startTime = req.getStartTime();
			long endTime = req.getEndTime();
			List<Object> idList = req.getIdList();
			int type = req.getType();//0�����շ�Ա���ͳ�� 1�����������ͳ�� 2������λ�α�Ų�ѯ 3������λ��ѯ
			if(startTime <= 0
					|| endTime <= 0
					|| idList == null
					|| idList.isEmpty()){
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			String column = null;
			if(type == 0){
				column = "uid";//���շ�Ա���ͳ��
			}else if(type == 1){
				column = "comid";//���������ͳ��
			}else if(type == 2){
				column = "berthseg_id";//����λ�α��ͳ��
			}else if(type == 3){
				column = "berth_id";//����λ���ͳ��
			}else if(type == 4){
				column = "groupid";
			}
			if(column == null){
				resp.setResult(-1);
				resp.setErrmsg("��������");
				return resp;
			}
			String preParams = "";
			for(int i = 0; i<idList.size(); i++){
				if(i == 0){
					preParams ="?";
				}else{
					preParams += ",?";
				}
			}
			List<Object> params = new ArrayList<Object>();
			params.add(0);
			params.add(startTime);
			params.add(endTime);
			params.add(0);//ͣ���ѣ���Ԥ����
			params.add(2);//׷��ͣ����
			params.add(3);//Ԥ��ͣ����
			params.add(4);//Ԥ���˿Ԥ����
			params.add(5);//Ԥ�����ɣ�Ԥ�����㣩
			params.addAll(idList);
			String sql = "select sum(amount) summoney,source,"+column+" from group_account_tb where " +
					" is_delete=? and create_time between ? and ? and source in (?,?,?,?,?) " +
					" and "+column+" in ("+preParams+") group by "+column+",source ";
			List<Map<String, Object>> list = readService.getAllMap(sql, params);
			if(list != null && !list.isEmpty()){
				List<Object> existIds = new ArrayList<Object>();//�б��Ѵ��ڵ�����
				List<StatsAccount> accounts = new ArrayList<StatsAccount>();
				for(Map<String, Object> map : list){
					Long id = (Long)map.get(column);
					Integer source = (Integer)map.get("source");
					Double summoney = Double.valueOf(map.get("summoney") + "");
					
					StatsAccount account = null;
					if(existIds.contains(id)){
						for(StatsAccount statsAccount : accounts){
							long statsId = statsAccount.getId();
							if(id.intValue() == statsId){//����ƥ�������
								account = statsAccount;
								break;
							}
						}
					}else{
						existIds.add(id);
						account = new StatsAccount();
						account.setId(id);
						accounts.add(account);//������
					}
					switch (source) {
					case 0://ͣ���ѣ���Ԥ����
						account.setParkingFee(summoney);
						break;
					case 2://׷��ͣ����
						account.setPursueFee(summoney);
						break;
					case 3://Ԥ��ͣ����
						account.setPrepayFee(summoney);
						break;
					case 4://Ԥ���˿Ԥ�����
						account.setRefundFee(summoney);
						break;
					case 5://Ԥ�����ɣ�Ԥ�����㣩
						account.setAddFee(summoney);
						break;
					default:
						break;
					}
				}
				resp.setAccounts(accounts);
				return resp;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		resp.setResult(-1);
		resp.setErrmsg("ϵͳ����");
		return resp;
	}
	
}