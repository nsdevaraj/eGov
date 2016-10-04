INSERT INTO EG_ACTION (ID,NAME,URL,QUERYPARAMS,PARENTMODULE,ORDERNUMBER,DISPLAYNAME,ENABLED,CONTEXTROOT,VERSION,CREATEDBY,CREATEDDATE,LASTMODIFIEDBY,LASTMODIFIEDDATE,APPLICATION) values  (NEXTVAL('SEQ_EG_ACTION'),'SearchSewerageCharges','/collectfee/search',null,(select id from EG_MODULE where name = 'SewerageTransactions'),1,'Collect Sewerage Charges',true,'stms',0,1,now(),1,now(),(select id from eg_module  where name = 'Sewerage Tax Management'));


INSERT INTO EG_ROLEACTION (roleid, actionid) values ((select id from eg_role where name = 'Collection Operator'),(select id from eg_action where name ='SearchSewerageCharges' and contextroot = 'stms'));
INSERT INTO EG_ROLEACTION (roleid, actionid) values ((select id from eg_role where name = 'Sewerage Tax Creator'),(select id from eg_action where name ='SearchSewerageCharges' and contextroot = 'stms'));