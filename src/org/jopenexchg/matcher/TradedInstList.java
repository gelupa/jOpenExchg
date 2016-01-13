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

public final class TradedInstList
{
	static final int INIT_CAPACITY = 2000;
	
	// stockid --> stock. IntegerÊÇÖ¤È¯´úÂë
	private HashMap<Integer, TradedInst> list = new HashMap<Integer, TradedInst>(INIT_CAPACITY);

	public final void addStock(TradedInst stock)
	{
		if(stock != null)
		{
			list.put(stock.stockid, stock);
		}
	}
	
	public final TradedInst getStock(int stockid)
	{
		return list.get(stockid);
	}
	
}
