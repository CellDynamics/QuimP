function [ out ] = HampelMeansmooth( in, params )
% apply Hampel filter on vector in
% params: [k sigma window]

coord = in;

[coordf,j] = hampel(coord,params(1),params(2));
hh = ~any(j')';
coordf = coordf(hh,:);
if hh(1)==0
    coordf = [coord(1,:); coordf];
end
if hh(end)==0
    coordf = [coordf; coord(end,:)];
end
coordff = meansmooth(coordf,[params(3),1]);
coordff = [coord(1,:); coordff; coord(end,:)];

out = coordff;

end

