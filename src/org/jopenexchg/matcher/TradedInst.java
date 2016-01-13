/* Java Open Exchange(jOpenExchg) Project
 *
 * Copyright (C) 2013  Alex Song
 *
 * This file is part of jOpenExchg.  
 *
 * jOpenExchg is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2 of the License, or (at your option) any later version.
 *
 * jOpenExchg is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General
 * Public License along with this library; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA 02111-1307, USA.
 */
package org.jopenexchg.matcher;

import java.util.*;

public final class TradedInst
{
	static final int UL_PRICE = Integer.MAX_VALUE;
	static final int LL_PRICE = 0;
	static final int NO_PRICE = 0;
	
	// 证券代码
	public int stockid = -1;
	
	// 证券简称
	public byte stockname[] = null;
	
	// 买入的价格队列. Long 是优先级而非价格
	public TreeMap<Long, PriceLeader> buyPrcList = null;
	
	// 卖出的价格队列. Long 是优先级而非价格
	public TreeMap<Long, PriceLeader> sellPrcList = null;
	
	// 最新行情情况. 都是根据订单的 ordPrc 字段而非 price 字段来的
	public long prevClsPrc = NO_PRICE;
	public long openPrc = NO_PRICE;
	public long highPrc = LL_PRICE;
	public long lowPrc = UL_PRICE;
	
	// 总成交量和成交金额
	public long totalValue = 0;
	public long totalAmount = 0;
	
	public TradedInst(int stockId, String stockName)
	{
		this.stockid = stockId;
		this.stockname = stockName.getBytes();
		
		buyPrcList = new TreeMap<Long, PriceLeader>();
		sellPrcList = new TreeMap<Long, PriceLeader>();
	}
	
	// 获得本方价位列表树
	public final TreeMap<Long, PriceLeader> getPrcList(boolean isBuy)
	{
		if(isBuy)
		{
			return buyPrcList;
		}
		else
		{
			return sellPrcList;
		}
	}
	
	// 获得对手方价位列表树
	public final TreeMap<Long, PriceLeader> getPeerPrcTree(boolean iAmBuy)
	{
		if(!iAmBuy)
		{
			return buyPrcList;
		}
		else
		{
			return sellPrcList;
		}
	}	
	
	// 向对应价位列表树上添加一个价位
	public final void addtoPrcList(boolean isBuy, PriceLeader prcLdr)
	{
		if(isBuy)
		{
			buyPrcList.put(prcLdr.prior, prcLdr);
		}
		else
		{
			sellPrcList.put(prcLdr.prior, prcLdr);
		}
	}
	

	/**
	 * 获取最优的对手方价位
	 * 
	 * @param iAmBuy: 本方是不是买
	 * @return null when does not exist such a peer prcldr
	 */
	public final Map.Entry<Long, PriceLeader> getBestPeerPrcLdr(boolean iAmBuy)
	{
		Map.Entry<Long, PriceLeader> bestPrcLdr = null;
		TreeMap<Long, PriceLeader> prcList = getPeerPrcTree(iAmBuy);
		
		bestPrcLdr = prcList.firstEntry();
		
		return bestPrcLdr;
	}
	
}
