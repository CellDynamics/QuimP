function disMap = buildDisMap(s, disWindow)
% DISPLACEMENTMAP Ceate a displacment map
    % disWindow defines the window size for calculating the displacment map
    % ([microns,seconds]). Gets converted to pixels per frame.

    M = size(s.motilityMap,2); % map width
    T = size(s.motilityMap,1); % total time in frames

    motMap = s.motilityMap;

    % build displacment map based on disWindow
    disMap = zeros(size(motMap));
    WH = round((disWindow(2)/s.FI)/2 ); %window height scale to frames from seconds, top and bottom
    
    disWindow(2) = (WH*2+1)*s.FI; % the actual time covered
    %fprintf('Actual window time: %.2f s (%d pixels)\n',disWindow(2), WH*2+1);
    
    for t = 1:T,

       scale = M / s.stats(t,8); % one micron equals so many pixels (stat 8=perimiter)
       WW =  round((disWindow(1)*scale)/2); % scale microns to pixels for this frame
                                            % half to get pixels either side
       %if(t==1), fprintf('%d-Actual window width: %.2f microns (%d pixels)\n',t,(WW*2+1)/scale, WW*2+1); end
       index = repmat((-WW:WW), WH+WH+1,1);

       tm = (t-1:-1:t-WH);
       tm = sum(tm>0);  % t minus
       tp = (t+1:t+WH);
       tp = sum(tp<=T);  % t minus
       
       % calc displacment within the window
       for i = 1:M,
           window = buildTrackMap(motMap,s.forwardMap,s.backwardMap, t,i, tp,tm,index);
           avSpeed = sum(window,2)./size(window,2);  %mean(window,2);
           displacement = cumsum(avSpeed.*s.FI); % convert to microns moved                                   
           disMap(t,i) = displacement(end); % calc gradient estimate, convert to micons per sec  
       end
    end
end


function m = buildTrackMap(map,forw,back, t,p,tp,tm, index),
    
    M = size(map,2);

    f=[];b=[];
    if(tp>0), 
        %f=trackForwAcc(originMap, coordMap,t,p,tp);
        f=trackForward(forw,t,p,tp);
    end
    if(tm>0), 
        b=trackBackward(back,t,p,tm);
    end
    
    v = [b',[t,p]',f']';
    vv = repmat(v(:,2),1,size(index,2));
  
    index = index(1:length(v),:)+vv;
    index(index<1) = M + index(index<1);
    index(index>M) = index(index>M)-M;

    m = zeros(length(v),size(index,2));
    for j = 1:length(v),
        m(j,:) = map(v(j,1),index(j,:));
    end

end
