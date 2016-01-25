%% hatfilter test generator
in = [1:40; zeros(1,40)];
in(:,19:21) = [19:21;1 1 1];
in = in';
plot(in(:,1),in(:,2),'-o'); grid on

% based on hatsmooth
crown = 5;
brim = 3;
sig = 0.05;

coord = in;
clear ratio
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

hold on
plot(coordpp(:,1),coordpp(:,2),'-rs')
hold off
figure
plot(ratio);grid on
hold on
plot(coordpp(:,1),repmat(sig,length(coordpp),1),'-g')
hold off
