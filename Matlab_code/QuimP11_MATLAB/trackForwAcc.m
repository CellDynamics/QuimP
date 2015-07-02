function [ path ] = trackForwAcc( originMap, coordMap ,  f ,p, T)

    path = zeros(T,2);  
    p = coordMap(f,p); 
    
    s = size(coordMap,2);
    
    for t=1:T,
          
        diffA = p - originMap(f+t,:);
        diffB = p - (originMap(f+t,:)-1) ;
        diffC = p - (originMap(f+t,:)+1) ;
          
        [minV(1), minI(1)] = min(abs(diffA));
        [minV(2), minI(2)] = min(abs(diffB));
        [minV(3), minI(3)] = min(abs(diffC));
 
          
        [theMin,index]=min(minV);
        index = minI(index);
  

        path(t,:) = [f+t, index];
       
        
        % interpolate to get a new p.
        if(theMin >= 0),
            ii = index+1;
            if(ii > s), ii = 1; end
   
            r = ratio(p,originMap(f+t,index), originMap(f+t,ii) );
            
            p = interpolate(r,coordMap(f+t,index), coordMap(f+t,ii) );
            
            
        else
            ii = index-1;
            if(ii < 1), ii = s; end
            r = ratio( p,originMap(ii),originMap(index) );
            p = interpolate( r,coordMap(ii),coordMap(index) );
            
        end
        
        
    end
    
    
end

function r = ratio(p,a,b)

    if( a > (b + 0.5)), % crossed zero (prob)
        b = b + 1;
    end

    r = (p-a)/(b-a);  

end

function v = interpolate(r,a,b)

    if( a > (b + 0.5)), % crossed zero (prob)
        b = b + 1;
    end

    v = a+((b-a)*r);
    

end