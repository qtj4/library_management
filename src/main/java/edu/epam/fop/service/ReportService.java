package edu.epam.fop.service;

import edu.epam.fop.model.OrderStatus;
import edu.epam.fop.dao.OrderDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReportService {

    private final OrderDao orderDao;

    @Autowired
    public ReportService(OrderDao orderDao) {
        this.orderDao = orderDao;
    }

    public long totalIssued(){
        try {
            return orderDao.findByStatus(OrderStatus.ISSUED).size();
        }catch(Exception e){ throw new RuntimeException(e);}
    }

    public List<Object[]> mostRequested(){
        try {
            var all = orderDao.findAll();
            java.util.Map<Long, Long> map = new java.util.HashMap<>();
            all.forEach(o->{ if(o.getCopy()!=null && o.getCopy().getBook()!=null){
                Long bid = o.getCopy().getBook().getId();
                map.put(bid, map.getOrDefault(bid,0L)+1);
            }});
            java.util.List<Object[]> list = new java.util.ArrayList<>();
            map.forEach((k,v)-> list.add(new Object[]{k,v}));
            list.sort((a,b)-> Long.compare((Long)b[1], (Long)a[1]));
            return list;
        }catch(Exception e){ throw new RuntimeException(e);}
    }
} 