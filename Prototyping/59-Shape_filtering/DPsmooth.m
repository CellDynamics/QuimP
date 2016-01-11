function [out, ix, ixx] = DPsmooth(in,params)
% apply Douglas-Peucker on vector in
% params: [iterations]

iter = params(1);

% addpath('dpsimplify/')
coord = in;
psin = coord;
for i=1:iter
    lin = 1:length(psin);
    [~,ix] = dpsimplify(psin,1/exp(-(i)*0.1));
    ixx = setdiff(lin,ix);
    psout = psin(ixx',:);
    psin = psout;
    psin = [coord(1,:); psin; coord(end,:)];

end
out = psin;