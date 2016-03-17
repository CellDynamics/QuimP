function [ originIndexesF, originIndexesB ] = buildTrackMaps( originMap, coordMap ),
% QuimP11 function
% BUILDTRACkMAPS   Builds matrices that allow tracking through maps.
%
%       [forwardMap , backwardMap] = buildTrackMaps(originMap, coordMap)
%
%       READQANALYSIS() builds these maps for you. See the QuimP11
%       documentation for an explanation of the forward/backward maps.
  
  originIndexesB = zeros(size(originMap));
  originIndexesF = zeros(size(originMap));
  
  minV = [0,0,0];
  minI = [0,0,0];
  
  %calc backward map
  for i = 2:size(originMap,1),
      
      for j = 1:size(originMap,2),
          p = originMap(i,j);
          
          diffA = abs(p - coordMap(i-1,:));
          diffB = abs(p - (coordMap(i-1,:)-1) );
          diffC = abs(p - (coordMap(i-1,:)+1) );
  
          %[minA, iA] = min(diffA);
          %[minB,iB ] = min(diffB);
          [minV(1), minI(1)] = min(diffA);
          [minV(2), minI(2)] = min(diffB);
          [minV(3), minI(3)] = min(diffC);
  
          %if (minA < minB),
              %originIndexesB(i,j) = iA;
          %else
              %originIndexesB(i,j) = iB;
          %end
          [~,index]=min(minV);
          originIndexesB(i,j) = minI(index);
          
      end
  
  end
  
  %calc forward map
  minV = [0,0,0];
  minI = [0,0,0];
  for i = 1:size(originMap,1)-1,
      
      for j = 1:size(originMap,2),
          p = coordMap(i,j);
          
          diffA = abs(p - originMap(i+1,:));
          diffB = abs(p - (originMap(i+1,:)-1) );
          diffC = abs(p - (originMap(i+1,:)+1) );
          
          [minV(1), minI(1)] = min(diffA);
          [minV(2), minI(2)] = min(diffB);
          [minV(3), minI(3)] = min(diffC);
          
          [~,index]=min(minV);
          originIndexesF(i,j) = minI(index);
          
      end
  
  end
  
end

