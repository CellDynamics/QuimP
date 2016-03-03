function out = hatsmooth(in,params)
% apply running median on vector in
% params: [window ile smooth level]

% warning - this implementation differs from filter implemented in java
% which is less buggy. See plugins.test.resources for more actual verison
w = params(1);
ile = params(2);
sm = params(3);
level = params(4);

coord = in;
wp = floor(w/2);
ind = padarray([1:length(coord)]',wp,'circular');

X = coord(:,1); Y = coord(:,2);
Xt = [X; X(1)];
Yt = [Y; Y(1)];
dx = diff(Xt); dy = diff(Yt);
P = sum(sqrt(dx.^2+dy.^2));
A = polyarea(X,Y);
circ = (4*pi*A)/(P.^2);

l = 1;
cc = [];
Pc = [];
for i=wp+1:length(coord)
    indtorem = ind(i-wp:i+wp);
    coordrem = coord;
    coordrem(indtorem,:) = [];
    A = polyarea(coordrem(:,1),coordrem(:,2));
    coordrem = [coordrem;coordrem(1,:)];
    d = diff(coordrem);
    P = sum(sqrt(sum(d.^2,2)));
    
    coordrem = coord(indtorem,:);
    
      % candidate 1
%     center = coordrem(wp+1,:);
%     d = coordrem - repmat(center,length(coordrem),1);
%     Pc = [Pc std(sqrt(sum(d.^2,2)))];
    
    % candidate 2
    center = coordrem(wp+1,:); %coordrem(wp+1,:);
    d = coordrem - repmat(center,length(coordrem),1);
    Pc = [Pc mean(sqrt(sum(d.^2,2)))];
    
    cc = [cc (4*pi*A)/(P.^2)];
end

cc = cc./(Pc);
cc = cc/circ;

ccsort = sort(cc,'descend')

if ccsort(1)>level
    coordrem = coord;
    clear indtorem;
    i = 1;
    found = 0;
    while(found<ile)
        if(i>length(ccsort))
            warning('Can find next candidate. Use smaller window');
            break;
        end
        m = find(cc==ccsort(i));    
        m = m + wp+1;
        if found>0
            sub = indtorem;   % all previous cases (indexes)
            mmsub = minmax(sub(:)');    % range of previous results
            mmcurr = minmax(ind(m-wp:m+wp)'); % current indexes (candidates)
            % check if current indexes are common with any of previous cases
            if mmcurr(2) < mmsub(1) % maximum current < min prev
                % found candidate
                found = found + 1;
                disp([find(cc==ccsort(i)) ccsort(i)])
                i = i + 1;
                indtorem(found,:) = ind(m-wp:m+wp);
            else
                if mmcurr(1) > mmsub(2) % min current > max prev
                    % found candidate
                    found = found + 1;
                    disp([find(cc==ccsort(i)) ccsort(i)])
                    i = i + 1;
                    indtorem(found,:) = ind(m-wp:m+wp);
                else
                    i = i + 1; % check next
                    continue;
                end
            end
        else
            disp([find(cc==ccsort(i)) ccsort(i)])
            i = i + 1; % for one accept it and go to next candidate
            found = found + 1;
            indtorem(found,:) = ind(m-wp:m+wp);
        end
        % verify if found protrusion is inside or ouside polygon
        % temporary remove just found indexes
        it = indtorem(found,:);
        tmppol = coord;
        tmppol(it,:) = [];
        tp = coord(it,:); % middle point of window
        if(any( inpolygon(tp(:,1),tp(:,2),tmppol(:,1),tmppol(:,2)) ))
            disp('ins');
            % delete found
            indtorem(found,:) = [];
            found = found - 1;
        end

    end
    coordrem(reshape(indtorem,1,[]),:) = [];
else
    coordrem = coord;
end

out(:,1) = medfilt1(coordrem(:,1),sm); % or smooth here
out(:,2) = medfilt1(coordrem(:,2),sm);
% [xDatax, yDatax] = prepareCurveData( [], coordrem(:,1) );
% [xDatay, yDatay] = prepareCurveData( [], coordrem(:,2) );
% ft = fittype( 'smoothingspline' );
% opts = fitoptions( 'Method', 'SmoothingSpline' );
% opts.SmoothingParam = sm;
% [fitresult, ~] = fit( xDatax, yDatax, ft, opts );
% out(:,1) = fitresult(xDatax);
% [fitresult, ~] = fit( xDatay, yDatay, ft, opts );
% out(:,2) = fitresult(xDatay);
