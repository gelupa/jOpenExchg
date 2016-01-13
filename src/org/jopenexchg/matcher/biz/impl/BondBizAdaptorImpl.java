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
package org.jopenexchg.matcher.biz.impl;

import org.jopenexchg.matcher.Order;
import org.jopenexchg.matcher.biz.BizAdaptor;

public final class BondBizAdaptorImpl implements BizAdaptor
{
	final static long MAX_PRIOR_ADJUST = 0;
	final static long PRIOR_SHIFT = 0;
	
	private final long calcBasePrior(boolean isbuy, long price)
	{
		long basePrior = 0;
		
		if(isbuy)
		{
			basePrior = -price;
		}
		else
		{
			basePrior = price;
		}
		
		return basePrior;		
	}
	
	@Override
	public final long calcPrior(Order order)
	{
		return calcBasePrior(order.isbuy, order.price);
	}

	/**
	 * 这里的实现并无出奇之处，但如果有特别的需求，则可以在移位的基础上加上调节量
	 */
	@Override
	public final long calcMaxPrior(boolean isbuy, long price)
	{
		return calcBasePrior(isbuy, price);
	}

	@Override
	public final long ordPrc2Price(long ordPrc)
	{
		return 10000 - ordPrc;
	}

	@Override
	public final long price2OrdPrc(long price) {
		// TODO Auto-generated method stub
		return 10000 - price;
	}

}
