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

import org.jopenexchg.hldg.*;

/**
*   撤单在内部是通过Order 的 引用来完成的。撤单的时候查找速度是O(1)
*   	撤单的时候只在订单上做一个标记并计减对应PrcLdr的累积总量，并不马上从队列中删除
*   	这是因为目前订单队列是一个双向链表，但JAVA内置的数据结构并不能根据对象直接很快在
*   	双向链表中定位到位置。这个工作可以DELAY到未来吃档位的时候来完成。
*   	
*   	另外一个加快速度的方式是PrcLdr上的累积总量到达0的时候就可以简单删除这个价格档位
**/

/**
 *
 * order.ordPrc --- stock/bond ---> 
 * order.price  --- b/s/else --> 
 * order.prior  ----------> PrcLdr.prior
 *
 * 注意有可能同一个买卖方向的不同price会对应到不同的prior
 */

public final class Order
{
	/**
	 * 以下内容是和订单刚收到的时候完全一样
	 */
	public short pbu = 0;
	public int reff = 0;

	public byte acctType = 'A';
	public int accNo = 0;
	public boolean isbuy = true;
	public int stockid = 0;
	public long ordQty = 0;
	public long ordPrc = 0;		// 行情揭示必须使用这个值。比如某些债券用收益率报价
	
	/**
	 * 以下的内容并不和提交时的订单内容完全一样，可变
	 * 	内部组织队列和撮合用的价格. 有些怪异的债券产品其行情报价可能用收益率
	 *	所以进来后会倒转一下，形成容易符合直觉的‘价格’
	 */
	public long price = 0;			
	public long remQty = 0;
	public boolean delflg = false;	// 订单删除标志，撤单时不用立刻从列表里面摘除

	/**
	 * CONTEXT 区域，采用 Lazy Loading 方式
	 * 
	 * lazy loading, 保证一个订单最多只查询一次持仓
	 * 		
	 * 		持仓库的查询和更新速度是很快的: 根据基准测试，更新是335W/s, 查询是709W/s
	 * 
	 * 		对于证券的卖出方，应该在前端检查的时候就检查余额同时设置此字段
	 * 		对于证券的买入方，则是在发生撮合匹配的时候才需要查询并同时设置此字段
	 * 
	 */
	public TradedInst stock = null;
	public Hldg hldg = null;	
	
	public String toString()
	{
		StringBuffer temp = new StringBuffer(256);
		
		temp.append("isBuy = ");
		temp.append(isbuy);	
		
		temp.append("; stockid = ");
		temp.append(stockid);
		
		temp.append("; ordPrice = ");
		temp.append(ordPrc);
		
		temp.append("; ordQty = ");
		temp.append(ordQty);
		
		temp.append("; remQty = ");
		temp.append(remQty);
		
		return temp.toString();
	}
	
}
