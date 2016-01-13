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

import org.jopenexchg.pool.WithId;


public final class PriceLeader implements WithId
{
	private int id = 0;
	
	// 优先级是数字越大越排在后面. 卖单应该价格越低优先级越高; 买单应该价格越高优先级越高
	public long prior = 0;
	
	public long ordPrc = 0;		// 和订单输入使用的行情揭示值对应
	public long price = 0;		// 和排队列时候的值对应。考虑某些债券可能用收益率作为行情报价
	public long accumQty = 0;	// 本价格档位上累计堆积了多少量. 累积量是0时价格档位马上回收
	
	public LinkedList<Order> orderList = new LinkedList<Order>();

	@Override
	public final int getId() 
	{
		return id;
	}

	@Override
	public final void setId(int id) 
	{
		this.id = id;
	}

}
