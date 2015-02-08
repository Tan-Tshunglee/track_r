package com.antilost.app.common;

public interface TrackRInitialize {

	/**
	 * ��ʼ�����Դ
	 */
	void initDataSource();
	
	/**
	 * ��ʼ���ؼ�
	 */
	void initWidget();
	
	/**
	 * ��ʼ���ؼ�״̬
	 */
	void initWidgetState();
	
	/**
	 * ��ʼ���ؼ�������
	 */
	void initWidgetListener();
	
	/**
	 * �ؼ���Ӽ�����
	 */
	void addWidgetListener();
}
