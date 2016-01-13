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
package org.jopenexchg.matcher.event.impl;

import org.jopenexchg.hldg.*;
import org.jopenexchg.matcher.Order;
import org.jopenexchg.matcher.event.EventHandler;

public final class EventHandlerHldgImpl implements EventHandler
{
	private HldgTable hldgTbl = null;
	private HldgKey key = null;

	public EventHandlerHldgImpl(HldgTable hldgTbl)
	{
		this.hldgTbl = hldgTbl;
		this.key = new HldgKey();
	}
	
	private final Hldg getRelatedHldg(Order order)
	{
		key.accNo = order.accNo;
		key.accType = order.acctType;
		key.hldgType = 0;
		key.pbu = order.pbu;
		key.stockid = order.stockid;
		
		return hldgTbl.getHldg(key);		
	}
	
	@Override
	public final void enterOrderBook(Order order)
	{
	}

	@Override
	public final void leaveOrderBook(Order order)
	{
	}

	/**
	 *  本函数只是用来测试因为引入持仓后顺带带来的HASH表查找和插入的速度影响
	 *  若唯出于性能测试目的，则不需要预先准备持仓
	 */
	@Override
	public final void match(Order newOrder, Order oldOrder, long matchQty,
			long matchPrice)
	{
		long value;
		
		if(oldOrder.hldg == null)
		{
			oldOrder.hldg = getRelatedHldg(oldOrder);
		}
		
		if(newOrder.hldg == null)
		{
			newOrder.hldg = getRelatedHldg(newOrder);
		}		
		
		if(oldOrder.isbuy)
		{
			oldOrder.hldg.A += matchQty;
			newOrder.hldg.A -= matchQty;
		}
		else
		{
			oldOrder.hldg.A -= matchQty;
			newOrder.hldg.A += matchQty;			
		}
		
		value = matchPrice * matchQty;
		
	}

	@Override
	public final void noMoreMatch(Order order)
	{
	}

	
	@Override
	public final void incomingOrder(Order order)
	{
	}

	@Override
	public final void callAuctionMatch(Order buyOrder, Order sellOrder, long matchQty,
			long matchPrice)
	{
		// TODO Auto-generated method stub
		
	}

	
	@Override
	public final void noMoreCallAuction(Order order)
	{
		// TODO Auto-generated method stub
		
	}

}
