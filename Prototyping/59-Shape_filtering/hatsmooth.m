function out = hatsmooth(in,params)
% apply running median on vector in
% params: [window iterations]

crown = params(1);
brim = params(2);
sig = params(3);
sm = params(4);

coord = in;

dp = floor(crown/2)+brim;
coordp = padarray(coord,crown+2*brim,'circular');
start = brim+floor(crown/2)+1;
indtoremove = cell(1,length(start:length(coord)+start-1));
for i=start:length(coord)+start-1
    allpoints = coordp(i-dp:i+dp,:);
    allvectors = diff(allpoints);
    lenallvectors = sum(sqrt(sum(allvectors.^2,2)));
    nocrownpoints = allpoints;
    nocrownpoints(brim+1:end-brim,:) = [];
    nocrownvectors = diff(nocrownpoints);
    lennocrownvectors = sum(sqrt(sum(nocrownvectors.^2,2)));
    ratio(i-start+1) = 1-lennocrownvectors/lenallvectors;    
end

for i=1:length(ratio)
    if(ratio(i))>sig
        indtoremove{i} = (i+start-1) - floor(crown/2):(i+start-1)+floor(crown/2);
    end
end

% set NaN for vertexes to remove
for i=1:length(indtoremove)
   if ~isempty(indtoremove{i})
       coordp(indtoremove{i},:) = NaN;
   end
end
% delete padding (on beginig)
coordp = coordp(crown+2*brim+1:end,:);
% and on the end
coordp = coordp(1:length(coord),:);
% find positions of NaNs (vertices to remove)
isnotnan = ~any(isnan(coordp),2);
% and remove them
coordpp = coordp(isnotnan,:);

[xDatax, yDatax] = prepareCurveData( [], coordpp(:,1) );
[xDatay, yDatay] = prepareCurveData( [], coordpp(:,2) );
ft = fittype( 'smoothingspline' );
opts = fitoptions( 'Method', 'SmoothingSpline' );
opts.SmoothingParam = sm;
[fitresult, ~] = fit( xDatax, yDatax, ft, opts );
out(:,1) = fitresult(xDatax);
[fitresult, ~] = fit( xDatay, yDatay, ft, opts );
out(:,2) = fitresult(xDatay);
