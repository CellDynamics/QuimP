function out = hatsmooth_deb(in,params)
% apply running median on vector in
% params: [window ile smooth level]

% this is version based on hatsmooth from Prototyping.
% java version is based on this one and agree in values
% java version may differ

w = params(1);
ile = params(2);
sm = params(3);
level = params(4);

coord = in;
wp = floor(w/2);
ind = padarray([1:length(coord)]',w,'circular','post');

X = coord(:,1); Y = coord(:,2);
Xt = [X; X(1)];
Yt = [Y; Y(1)];
dx = diff(Xt); dy = diff(Yt);
P = sum(sqrt(dx.^2+dy.^2));
A = polyarea(X,Y);
circ = (4*pi*A)/(P.^2)

l = 1;
cc = [];
Pc = [];
for i=1:length(coord)
    indtorem = ind(i:i+w-1);
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
    center = coordrem(1,:); %coordrem(wp+1,:);
    d = coordrem(1,:) - coordrem(end,:);
    Pc = [Pc mean(sqrt(sum(d.^2,2)))];
    
    retcoordrem{i} = coordrem;% the same values for window range in java
    retPC{i} = Pc(end); % the same weighting
    
    % currently java uses different veighting
    
    cc = [cc (4*pi*A)/(P.^2)];
    retcc{i} = cc(end);
    retccw{i} = cc(end)/Pc(end)/circ;
end

cc = cc./(Pc);
cc = cc/circ;

ccsort = sort(cc,'descend') % the same in java log cirs

if ccsort(1)>level
    coordrem = coord;
    clear indtorem;
    i = 1;
    found = 0;
    disp(['Process ' num2str(i)])
    while(found<ile)
        if(i>length(ccsort))
            warning('Can find next candidate. Use smaller window');
            break;
        end
        m = find(cc==ccsort(i));    
        if found>0
            sub = indtorem;   % all previous cases (indexes)
            mmsub = minmax(sub);    % range of previous results
            mmcurr = minmax(ind(m:m+w-1)'); % current indexes (candidates) - THIS is difference to java, here some windows can ha indexes 1-max when they are on beginning or end of points
            % check if current indexes are common with any of previous cases
            if mycontains(mmsub,mmcurr)==1 
                % found candidate
                found = found + 1;
                disp(['add ' num2str(i)])
                disp([find(cc==ccsort(i)) ccsort(i)])
                i = i + 1;
                indtorem(found,:) = ind(m:m+w-1);
            else
                disp(['Skip ' num2str(i)])
                i = i + 1; % check next
                continue;
            end
        else
            disp(['add ' num2str(i)])
            disp([find(cc==ccsort(i)) ccsort(i)])
            i = i + 1; % for one accept it and go to next candidate
            found = found + 1;
            indtorem(found,:) = ind(m:m+w-1);
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
    indtorem
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
assignin('base','retcoordrem',retcoordrem);
assignin('base','retcc',retcc);
assignin('base','retPC',retPC);
assignin('base','retccw',retccw);
end

function ret=mycontains(mm,cur)

x = zeros(1:size(mm,1));
cor = sort(cur);
for i=1:size(mm,1)
    c = mm(i,:);
    if cur(1)>c(2)
        x(i) = 1;
    else
        if cur(2)<c(1)
            x(i) = 1;
        end
    end
end
ret = all(x);
end