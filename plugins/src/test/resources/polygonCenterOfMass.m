function [ out ] = polygonCenterOfMass( xy )
%polygonCenterOfMass Matlab version of function from
%   http://www.shodor.org/~jmorrell/interactivate/org/shodor/util11/PolygonUtils.java
    N = size(xy,1);
    cx = 0; cy = 0;
    
    xy1 = [xy ; xy(1,:)];
    A = polyarea(xy1(:,1),xy1(:,2));
    factor = 0;
    for i=1:N
       j = i+1;
       if j>N
           j = j-N;
       end
       factor = xy(i,1)*xy(j,2) - xy(j,1)*xy(i,2);
       cx = cx + (xy(i,1) + xy(j,1)) * factor;
       cy = cy + (xy(i,2) + xy(j,2)) * factor;
    end
    
    factor = 1/(6*A);
    cx = cx * factor;
    cy = cy * factor;
    
    out = [abs(cx) abs(cy)];

end
