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

import org.jopenexchg.matcher.Order;
import org.jopenexchg.matcher.event.EventHandler;

public final class EventHandlerDebugImpl implements EventHandler
{

	@Override
	public void enterOrderBook(final Order order)
	{
		System.out.println("");
		System.out.println("[enterOrderBook]");
		System.out.println(" + " + order);
	}

	@Override
	public void leaveOrderBook(final Order order)
	{
		System.out.println("");
		System.out.println("[leaveOrderBook]");
		System.out.println(" - " + order);
	}

	@Override
	public void match(final Order newOrder, final Order oldOrder, long matchQty, long matchPrice)
	{
		System.out.println("");
		System.out.println("[match]: matchPrice = " + matchPrice + " matchQty = " + matchQty);
		System.out.println("     new " + newOrder);
		System.out.println("     old " + oldOrder);
	}

	@Override
	public void noMoreMatch(final Order order)
	{
		System.out.println("");
		System.out.println("[noMoreMatch]");
		System.out.println(" + " + order);
	}

	
	@Override
	public void incomingOrder(final Order order)
	{
		System.out.println("------------------------");
		System.out.println("[incomingOrder]");
		System.out.println(" + " + order);		
	}

	
	@Override
	public void callAuctionMatch(final Order buyOrder, final Order sellOrder, long matchQty,
			long matchPrice)
	{
		System.out.println("");
		System.out.println("[ocall match]: matchPrice = " + matchPrice + " matchQty = " + matchQty);
		System.out.println("     buy " + buyOrder);
		System.out.println("     sell " + sellOrder);
		
	}

	
	@Override
	public void noMoreCallAuction(Order order)
	{
		// TODO Auto-generated method stub
		
	}

}
